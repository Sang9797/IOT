import React, { useState, useEffect } from 'react';
import { 
  Plus, 
  Search, 
  Filter, 
  Edit, 
  Trash2, 
  Power, 
  PowerOff, 
  Settings,
  Eye,
  Download,
  Upload
} from 'lucide-react';
import useDeviceStore from '../../store/deviceStore';
import DeviceForm from '../../components/Devices/DeviceForm';
import DeviceDetails from '../../components/Devices/DeviceDetails';

const DeviceManagement = () => {
  const {
    devices,
    selectedDevice,
    isLoading,
    error,
    filters,
    pagination,
    fetchDevices,
    createDevice,
    updateDevice,
    deleteDevice,
    updateDeviceStatus,
    setFilters,
    clearFilters,
    setPagination,
    getFilteredDevices,
  } = useDeviceStore();

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showEditForm, setShowEditForm] = useState(false);
  const [showDetails, setShowDetails] = useState(false);
  const [editingDevice, setEditingDevice] = useState(null);
  const [selectedDevices, setSelectedDevices] = useState([]);
  const [showFilters, setShowFilters] = useState(false);

  useEffect(() => {
    fetchDevices();
  }, [fetchDevices]);

  const handleCreateDevice = async (deviceData) => {
    const result = await createDevice(deviceData);
    if (result.success) {
      setShowCreateForm(false);
    }
  };

  const handleUpdateDevice = async (deviceData) => {
    const result = await updateDevice(editingDevice.id, deviceData);
    if (result.success) {
      setShowEditForm(false);
      setEditingDevice(null);
    }
  };

  const handleDeleteDevice = async (deviceId) => {
    if (window.confirm('Are you sure you want to delete this device?')) {
      await deleteDevice(deviceId);
    }
  };

  const handleStatusChange = async (deviceId, status) => {
    await updateDeviceStatus(deviceId, status);
  };

  const handleEditDevice = (device) => {
    setEditingDevice(device);
    setShowEditForm(true);
  };

  const handleViewDevice = (device) => {
    setEditingDevice(device);
    setShowDetails(true);
  };

  const handleSelectDevice = (deviceId) => {
    setSelectedDevices(prev =>
      prev.includes(deviceId)
        ? prev.filter(id => id !== deviceId)
        : [...prev, deviceId]
    );
  };

  const handleSelectAll = () => {
    const filteredDevices = getFilteredDevices();
    setSelectedDevices(
      selectedDevices.length === filteredDevices.length
        ? []
        : filteredDevices.map(device => device.id)
    );
  };

  const handleBulkAction = async (action) => {
    if (selectedDevices.length === 0) return;

    switch (action) {
      case 'delete':
        if (window.confirm(`Are you sure you want to delete ${selectedDevices.length} devices?`)) {
          for (const deviceId of selectedDevices) {
            await deleteDevice(deviceId);
          }
          setSelectedDevices([]);
        }
        break;
      case 'online':
        for (const deviceId of selectedDevices) {
          await updateDeviceStatus(deviceId, 'ONLINE');
        }
        setSelectedDevices([]);
        break;
      case 'offline':
        for (const deviceId of selectedDevices) {
          await updateDeviceStatus(deviceId, 'OFFLINE');
        }
        setSelectedDevices([]);
        break;
      default:
        break;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ONLINE':
        return 'status-online';
      case 'OFFLINE':
        return 'status-offline';
      case 'MAINTENANCE':
        return 'status-maintenance';
      case 'ERROR':
        return 'status-error';
      default:
        return 'badge-gray';
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'SENSOR':
        return 'badge-info';
      case 'ACTUATOR':
        return 'badge-success';
      case 'CONTROLLER':
        return 'badge-warning';
      case 'MONITOR':
        return 'badge-error';
      default:
        return 'badge-gray';
    }
  };

  const filteredDevices = getFilteredDevices();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Device Management</h1>
          <p className="text-gray-600">Manage and monitor your IoT devices</p>
        </div>
        <div className="flex items-center space-x-3">
          <button className="btn-outline">
            <Download className="h-4 w-4 mr-2" />
            Export
          </button>
          <button className="btn-outline">
            <Upload className="h-4 w-4 mr-2" />
            Import
          </button>
          <button
            onClick={() => setShowCreateForm(true)}
            className="btn-primary"
          >
            <Plus className="h-4 w-4 mr-2" />
            Add Device
          </button>
        </div>
      </div>

      {/* Filters and Search */}
      <div className="card p-6">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between space-y-4 lg:space-y-0">
          <div className="flex flex-col sm:flex-row sm:items-center space-y-2 sm:space-y-0 sm:space-x-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search devices..."
                value={filters.search}
                onChange={(e) => setFilters({ search: e.target.value })}
                className="pl-10 input w-64"
              />
            </div>
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="btn-outline"
            >
              <Filter className="h-4 w-4 mr-2" />
              Filters
            </button>
            {Object.values(filters).some(f => f) && (
              <button
                onClick={clearFilters}
                className="btn-outline text-red-600 border-red-300 hover:bg-red-50"
              >
                Clear Filters
              </button>
            )}
          </div>

          <div className="flex items-center space-x-2">
            <span className="text-sm text-gray-500">
              {filteredDevices.length} of {devices.length} devices
            </span>
          </div>
        </div>

        {/* Advanced Filters */}
        {showFilters && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="label">Factory</label>
                <select
                  value={filters.factoryId}
                  onChange={(e) => setFilters({ factoryId: e.target.value })}
                  className="input"
                >
                  <option value="">All Factories</option>
                  <option value="factory-001">Factory 001</option>
                  <option value="factory-002">Factory 002</option>
                  <option value="factory-003">Factory 003</option>
                </select>
              </div>
              <div>
                <label className="label">Status</label>
                <select
                  value={filters.status}
                  onChange={(e) => setFilters({ status: e.target.value })}
                  className="input"
                >
                  <option value="">All Status</option>
                  <option value="ONLINE">Online</option>
                  <option value="OFFLINE">Offline</option>
                  <option value="MAINTENANCE">Maintenance</option>
                  <option value="ERROR">Error</option>
                </select>
              </div>
              <div>
                <label className="label">Type</label>
                <select
                  value={filters.type}
                  onChange={(e) => setFilters({ type: e.target.value })}
                  className="input"
                >
                  <option value="">All Types</option>
                  <option value="SENSOR">Sensor</option>
                  <option value="ACTUATOR">Actuator</option>
                  <option value="CONTROLLER">Controller</option>
                  <option value="MONITOR">Monitor</option>
                </select>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Bulk Actions */}
      {selectedDevices.length > 0 && (
        <div className="card p-4 bg-blue-50 border-blue-200">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-blue-900">
              {selectedDevices.length} device(s) selected
            </span>
            <div className="flex items-center space-x-2">
              <button
                onClick={() => handleBulkAction('online')}
                className="btn-sm bg-green-600 text-white hover:bg-green-700"
              >
                <Power className="h-3 w-3 mr-1" />
                Set Online
              </button>
              <button
                onClick={() => handleBulkAction('offline')}
                className="btn-sm bg-gray-600 text-white hover:bg-gray-700"
              >
                <PowerOff className="h-3 w-3 mr-1" />
                Set Offline
              </button>
              <button
                onClick={() => handleBulkAction('delete')}
                className="btn-sm bg-red-600 text-white hover:bg-red-700"
              >
                <Trash2 className="h-3 w-3 mr-1" />
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Devices Table */}
      <div className="card">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left">
                  <input
                    type="checkbox"
                    checked={selectedDevices.length === filteredDevices.length && filteredDevices.length > 0}
                    onChange={handleSelectAll}
                    className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Device
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Factory
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Last Seen
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {isLoading ? (
                <tr>
                  <td colSpan="7" className="px-6 py-12 text-center">
                    <div className="flex items-center justify-center">
                      <div className="loading-dots">
                        <div></div>
                        <div></div>
                        <div></div>
                        <div></div>
                      </div>
                    </div>
                  </td>
                </tr>
              ) : filteredDevices.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-6 py-12 text-center text-gray-500">
                    No devices found
                  </td>
                </tr>
              ) : (
                filteredDevices.map((device) => (
                  <tr key={device.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <input
                        type="checkbox"
                        checked={selectedDevices.includes(device.id)}
                        onChange={() => handleSelectDevice(device.id)}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-10 w-10">
                          <div className="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                            <Settings className="h-5 w-5 text-gray-600" />
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">{device.name}</div>
                          <div className="text-sm text-gray-500">{device.address}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={getTypeColor(device.type)}>
                        {device.type}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={getStatusColor(device.status)}>
                        {device.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {device.factoryId || 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {device.lastSeen ? new Date(device.lastSeen).toLocaleString() : 'Never'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => handleViewDevice(device)}
                          className="text-primary-600 hover:text-primary-900"
                          title="View Details"
                        >
                          <Eye className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleEditDevice(device)}
                          className="text-gray-600 hover:text-gray-900"
                          title="Edit Device"
                        >
                          <Edit className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleStatusChange(device.id, device.status === 'ONLINE' ? 'OFFLINE' : 'ONLINE')}
                          className={device.status === 'ONLINE' ? 'text-red-600 hover:text-red-900' : 'text-green-600 hover:text-green-900'}
                          title={device.status === 'ONLINE' ? 'Set Offline' : 'Set Online'}
                        >
                          {device.status === 'ONLINE' ? <PowerOff className="h-4 w-4" /> : <Power className="h-4 w-4" />}
                        </button>
                        <button
                          onClick={() => handleDeleteDevice(device.id)}
                          className="text-red-600 hover:text-red-900"
                          title="Delete Device"
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

      {/* Pagination */}
      <div className="flex items-center justify-between">
        <div className="text-sm text-gray-700">
          Showing {((pagination.page - 1) * pagination.limit) + 1} to {Math.min(pagination.page * pagination.limit, pagination.total)} of {pagination.total} results
        </div>
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setPagination({ page: pagination.page - 1 })}
            disabled={pagination.page === 1}
            className="btn-outline disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Previous
          </button>
          <span className="text-sm text-gray-700">
            Page {pagination.page} of {Math.ceil(pagination.total / pagination.limit)}
          </span>
          <button
            onClick={() => setPagination({ page: pagination.page + 1 })}
            disabled={pagination.page >= Math.ceil(pagination.total / pagination.limit)}
            className="btn-outline disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Next
          </button>
        </div>
      </div>

      {/* Modals */}
      {showCreateForm && (
        <DeviceForm
          isOpen={showCreateForm}
          onClose={() => setShowCreateForm(false)}
          onSubmit={handleCreateDevice}
          title="Create New Device"
        />
      )}

      {showEditForm && editingDevice && (
        <DeviceForm
          isOpen={showEditForm}
          onClose={() => {
            setShowEditForm(false);
            setEditingDevice(null);
          }}
          onSubmit={handleUpdateDevice}
          device={editingDevice}
          title="Edit Device"
        />
      )}

      {showDetails && editingDevice && (
        <DeviceDetails
          isOpen={showDetails}
          onClose={() => {
            setShowDetails(false);
            setEditingDevice(null);
          }}
          device={editingDevice}
        />
      )}
    </div>
  );
};

export default DeviceManagement;
