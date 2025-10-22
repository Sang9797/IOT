import React, { useState, useEffect } from 'react';
import { 
  Bell, 
  AlertTriangle, 
  CheckCircle, 
  XCircle, 
  Clock,
  Search,
  Filter,
  Eye,
  Trash2,
  RefreshCw,
  Download
} from 'lucide-react';
import { useDeviceStore } from '../../store/deviceStore';
import webSocketService from '../../services/websocket';
import { toast } from 'react-hot-toast';

const AlertsDashboard = () => {
  const { devices } = useDeviceStore();
  const [alerts, setAlerts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState({
    severity: '',
    status: '',
    deviceId: '',
    search: '',
  });

  useEffect(() => {
    // Load alerts from localStorage or API
    const savedAlerts = localStorage.getItem('alerts');
    if (savedAlerts) {
      setAlerts(JSON.parse(savedAlerts));
    }

    // Set up WebSocket connection for real-time alerts
    const token = localStorage.getItem('authToken');
    if (token) {
      webSocketService.connect(token);
    }

    // WebSocket event listeners
    const handleAlert = (data) => {
      console.log('Real-time alert received:', data);
      
      const newAlert = {
        id: Date.now(),
        deviceId: data.deviceId,
        deviceName: devices.find(d => d.id === data.deviceId)?.name || 'Unknown Device',
        type: data.type || 'SYSTEM',
        severity: data.severity || 'MEDIUM',
        message: data.message || 'System alert',
        status: 'ACTIVE',
        timestamp: new Date().toISOString(),
        acknowledged: false,
        acknowledgedBy: null,
        acknowledgedAt: null,
        metadata: data.metadata || {},
      };

      setAlerts(prev => [newAlert, ...prev]);
      localStorage.setItem('alerts', JSON.stringify([newAlert, ...alerts]));
      
      // Show toast notification
      toast(`Alert: ${newAlert.message}`, {
        duration: 5000,
        icon: getSeverityIcon(newAlert.severity),
        style: {
          background: getSeverityColor(newAlert.severity),
          color: 'white',
        },
      });
    };

    webSocketService.on('alert', handleAlert);

    return () => {
      webSocketService.off('alert', handleAlert);
    };
  }, [devices, alerts]);

  const getSeverityIcon = (severity) => {
    switch (severity) {
      case 'LOW':
        return '🔵';
      case 'MEDIUM':
        return '🟡';
      case 'HIGH':
        return '🟠';
      case 'CRITICAL':
        return '🔴';
      default:
        return '🔔';
    }
  };

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'LOW':
        return '#3b82f6';
      case 'MEDIUM':
        return '#f59e0b';
      case 'HIGH':
        return '#ef4444';
      case 'CRITICAL':
        return '#dc2626';
      default:
        return '#6b7280';
    }
  };

  const getSeverityBadge = (severity) => {
    switch (severity) {
      case 'LOW':
        return 'severity-low';
      case 'MEDIUM':
        return 'severity-medium';
      case 'HIGH':
        return 'severity-high';
      case 'CRITICAL':
        return 'severity-critical';
      default:
        return 'badge-gray';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'ACTIVE':
        return <AlertTriangle className="h-4 w-4 text-red-500" />;
      case 'ACKNOWLEDGED':
        return <CheckCircle className="h-4 w-4 text-yellow-500" />;
      case 'RESOLVED':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'DISMISSED':
        return <XCircle className="h-4 w-4 text-gray-500" />;
      default:
        return <Clock className="h-4 w-4 text-gray-500" />;
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
        return 'badge-error';
      case 'ACKNOWLEDGED':
        return 'badge-warning';
      case 'RESOLVED':
        return 'badge-success';
      case 'DISMISSED':
        return 'badge-gray';
      default:
        return 'badge-gray';
    }
  };

  const handleAcknowledge = (alertId) => {
    setAlerts(prev => {
      const updated = prev.map(alert => 
        alert.id === alertId 
          ? { 
              ...alert, 
              status: 'ACKNOWLEDGED',
              acknowledged: true,
              acknowledgedBy: 'Current User',
              acknowledgedAt: new Date().toISOString()
            }
          : alert
      );
      localStorage.setItem('alerts', JSON.stringify(updated));
      return updated;
    });
    toast.success('Alert acknowledged');
  };

  const handleResolve = (alertId) => {
    setAlerts(prev => {
      const updated = prev.map(alert => 
        alert.id === alertId 
          ? { 
              ...alert, 
              status: 'RESOLVED',
              resolvedAt: new Date().toISOString()
            }
          : alert
      );
      localStorage.setItem('alerts', JSON.stringify(updated));
      return updated;
    });
    toast.success('Alert resolved');
  };

  const handleDismiss = (alertId) => {
    setAlerts(prev => {
      const updated = prev.map(alert => 
        alert.id === alertId 
          ? { 
              ...alert, 
              status: 'DISMISSED',
              dismissedAt: new Date().toISOString()
            }
          : alert
      );
      localStorage.setItem('alerts', JSON.stringify(updated));
      return updated;
    });
    toast.success('Alert dismissed');
  };

  const handleDelete = (alertId) => {
    if (window.confirm('Are you sure you want to delete this alert?')) {
      setAlerts(prev => {
        const updated = prev.filter(alert => alert.id !== alertId);
        localStorage.setItem('alerts', JSON.stringify(updated));
        return updated;
      });
      toast.success('Alert deleted');
    }
  };

  const clearAllAlerts = () => {
    if (window.confirm('Are you sure you want to clear all alerts?')) {
      setAlerts([]);
      localStorage.removeItem('alerts');
      toast.success('All alerts cleared');
    }
  };

  const filteredAlerts = alerts.filter(alert => {
    if (filters.severity && alert.severity !== filters.severity) return false;
    if (filters.status && alert.status !== filters.status) return false;
    if (filters.deviceId && alert.deviceId !== filters.deviceId) return false;
    if (filters.search && !alert.message.toLowerCase().includes(filters.search.toLowerCase())) return false;
    return true;
  });

  const activeAlerts = alerts.filter(a => a.status === 'ACTIVE').length;
  const criticalAlerts = alerts.filter(a => a.severity === 'CRITICAL' && a.status === 'ACTIVE').length;
  const acknowledgedAlerts = alerts.filter(a => a.status === 'ACKNOWLEDGED').length;
  const resolvedAlerts = alerts.filter(a => a.status === 'RESOLVED').length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Alerts Dashboard</h1>
          <p className="text-gray-600">Monitor and manage system alerts and notifications</p>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="btn-outline"
          >
            <Filter className="h-4 w-4 mr-2" />
            Filters
          </button>
          <button
            onClick={clearAllAlerts}
            className="btn-outline text-red-600 border-red-300 hover:bg-red-50"
          >
            <Trash2 className="h-4 w-4 mr-2" />
            Clear All
          </button>
          <button className="btn-outline">
            <Download className="h-4 w-4 mr-2" />
            Export
          </button>
        </div>
      </div>

      {/* Alert Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-red-100 rounded-lg">
              <AlertTriangle className="h-6 w-6 text-red-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Active Alerts</p>
              <p className="text-2xl font-bold text-gray-900">{activeAlerts}</p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-orange-100 rounded-lg">
              <AlertTriangle className="h-6 w-6 text-orange-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Critical Alerts</p>
              <p className="text-2xl font-bold text-gray-900">{criticalAlerts}</p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-yellow-100 rounded-lg">
              <CheckCircle className="h-6 w-6 text-yellow-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Acknowledged</p>
              <p className="text-2xl font-bold text-gray-900">{acknowledgedAlerts}</p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-green-100 rounded-lg">
              <CheckCircle className="h-6 w-6 text-green-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Resolved</p>
              <p className="text-2xl font-bold text-gray-900">{resolvedAlerts}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Filters */}
      {showFilters && (
        <div className="card p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Filters</h3>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="label">Severity</label>
              <select
                value={filters.severity}
                onChange={(e) => setFilters({ ...filters, severity: e.target.value })}
                className="input"
              >
                <option value="">All Severities</option>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
            <div>
              <label className="label">Status</label>
              <select
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                className="input"
              >
                <option value="">All Status</option>
                <option value="ACTIVE">Active</option>
                <option value="ACKNOWLEDGED">Acknowledged</option>
                <option value="RESOLVED">Resolved</option>
                <option value="DISMISSED">Dismissed</option>
              </select>
            </div>
            <div>
              <label className="label">Device</label>
              <select
                value={filters.deviceId}
                onChange={(e) => setFilters({ ...filters, deviceId: e.target.value })}
                className="input"
              >
                <option value="">All Devices</option>
                {devices.map(device => (
                  <option key={device.id} value={device.id}>
                    {device.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Search</label>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search alerts..."
                  value={filters.search}
                  onChange={(e) => setFilters({ ...filters, search: e.target.value })}
                  className="pl-10 input"
                />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Alerts Table */}
      <div className="card">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-semibold text-gray-900">Alerts</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Alert
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Device
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Severity
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Timestamp
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredAlerts.length === 0 ? (
                <tr>
                  <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                    No alerts found
                  </td>
                </tr>
              ) : (
                filteredAlerts.map((alert) => (
                  <tr key={alert.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-start">
                        <span className="text-lg mr-3">
                          {getSeverityIcon(alert.severity)}
                        </span>
                        <div>
                          <div className="text-sm font-medium text-gray-900">
                            {alert.type}
                          </div>
                          <div className="text-sm text-gray-500">
                            {alert.message}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{alert.deviceName}</div>
                      <div className="text-sm text-gray-500">{alert.deviceId}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getSeverityBadge(alert.severity)}`}>
                        {alert.severity}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusBadge(alert.status)}`}>
                        {getStatusIcon(alert.status)}
                        <span className="ml-1">{alert.status}</span>
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(alert.timestamp).toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex items-center space-x-2">
                        {alert.status === 'ACTIVE' && (
                          <>
                            <button
                              onClick={() => handleAcknowledge(alert.id)}
                              className="text-yellow-600 hover:text-yellow-900"
                              title="Acknowledge"
                            >
                              <CheckCircle className="h-4 w-4" />
                            </button>
                            <button
                              onClick={() => handleResolve(alert.id)}
                              className="text-green-600 hover:text-green-900"
                              title="Resolve"
                            >
                              <CheckCircle className="h-4 w-4" />
                            </button>
                          </>
                        )}
                        {alert.status === 'ACKNOWLEDGED' && (
                          <button
                            onClick={() => handleResolve(alert.id)}
                            className="text-green-600 hover:text-green-900"
                            title="Resolve"
                          >
                            <CheckCircle className="h-4 w-4" />
                          </button>
                        )}
                        <button
                          onClick={() => handleDismiss(alert.id)}
                          className="text-gray-600 hover:text-gray-900"
                          title="Dismiss"
                        >
                          <XCircle className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(alert.id)}
                          className="text-red-600 hover:text-red-900"
                          title="Delete"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AlertsDashboard;
