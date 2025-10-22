import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { 
  Send, 
  Play, 
  Pause, 
  RotateCcw, 
  Settings, 
  Target, 
  Zap,
  Clock,
  CheckCircle,
  XCircle,
  AlertCircle,
  Search,
  Filter
} from 'lucide-react';
import { useDeviceStore } from '../../store/deviceStore';
import { controlAPI } from '../../services/api';
import { toast } from 'react-hot-toast';

const ControlCommands = () => {
  const { devices, fetchDevices } = useDeviceStore();
  const [selectedDevice, setSelectedDevice] = useState(null);
  const [commandHistory, setCommandHistory] = useState([]);
  const [isExecuting, setIsExecuting] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState({
    deviceId: '',
    status: '',
    type: '',
  });

  const { register, handleSubmit, formState: { errors }, reset } = useForm();

  useEffect(() => {
    fetchDevices();
    // Load command history from localStorage or API
    const savedHistory = localStorage.getItem('commandHistory');
    if (savedHistory) {
      setCommandHistory(JSON.parse(savedHistory));
    }
  }, [fetchDevices]);

  const onSubmit = async (data) => {
    setIsExecuting(true);
    
    try {
      let response;
      
      if (data.commandType === 'broadcast') {
        // Send broadcast command to all devices
        response = await controlAPI.sendBroadcastCommand({
          command: data.command,
          parameters: data.parameters ? JSON.parse(data.parameters) : {},
          timestamp: new Date().toISOString(),
        });
      } else {
        // Send command to specific device
        if (!selectedDevice) {
          toast.error('Please select a device');
          return;
        }
        
        response = await controlAPI.sendDeviceCommand(selectedDevice.id, {
          command: data.command,
          parameters: data.parameters ? JSON.parse(data.parameters) : {},
          timestamp: new Date().toISOString(),
        });
      }

      // Add to command history
      const newCommand = {
        id: Date.now(),
        deviceId: data.commandType === 'broadcast' ? 'ALL' : selectedDevice.id,
        deviceName: data.commandType === 'broadcast' ? 'All Devices' : selectedDevice.name,
        command: data.command,
        parameters: data.parameters,
        status: 'SENT',
        timestamp: new Date().toISOString(),
        response: response.data,
      };
      
      setCommandHistory(prev => [newCommand, ...prev]);
      localStorage.setItem('commandHistory', JSON.stringify([newCommand, ...commandHistory]));
      
      toast.success('Command sent successfully!');
      reset();
    } catch (error) {
      console.error('Command execution error:', error);
      toast.error('Failed to send command');
    } finally {
      setIsExecuting(false);
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'SENT':
        return <Clock className="h-4 w-4 text-yellow-500" />;
      case 'SUCCESS':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'FAILED':
        return <XCircle className="h-4 w-4 text-red-500" />;
      default:
        return <AlertCircle className="h-4 w-4 text-gray-500" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'SENT':
        return 'badge-warning';
      case 'SUCCESS':
        return 'badge-success';
      case 'FAILED':
        return 'badge-error';
      default:
        return 'badge-gray';
    }
  };

  const filteredHistory = commandHistory.filter(cmd => {
    if (filters.deviceId && cmd.deviceId !== filters.deviceId) return false;
    if (filters.status && cmd.status !== filters.status) return false;
    if (filters.type && cmd.command !== filters.type) return false;
    return true;
  });

  const onlineDevices = devices.filter(d => d.status === 'ONLINE');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Control Commands</h1>
          <p className="text-gray-600">Send commands to IoT devices and monitor execution</p>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="btn-outline"
          >
            <Filter className="h-4 w-4 mr-2" />
            Filters
          </button>
        </div>
      </div>

      {/* Command Form */}
      <div className="card p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Send Command</h3>
        
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Command Type */}
            <div>
              <label className="label">Command Type</label>
              <select
                {...register('commandType', { required: 'Command type is required' })}
                className={`input ${errors.commandType ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
              >
                <option value="device">Single Device</option>
                <option value="broadcast">Broadcast to All</option>
              </select>
            </div>

            {/* Device Selection */}
            <div>
              <label className="label">Target Device</label>
              <select
                {...register('deviceId')}
                onChange={(e) => {
                  const device = devices.find(d => d.id === e.target.value);
                  setSelectedDevice(device);
                }}
                className="input"
                disabled={!onlineDevices.length}
              >
                <option value="">Select a device</option>
                {onlineDevices.map(device => (
                  <option key={device.id} value={device.id}>
                    {device.name} ({device.address})
                  </option>
                ))}
              </select>
              {!onlineDevices.length && (
                <p className="text-sm text-red-600 mt-1">No online devices available</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Command */}
            <div>
              <label className="label">Command *</label>
              <select
                {...register('command', { required: 'Command is required' })}
                className={`input ${errors.command ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
              >
                <option value="">Select command</option>
                <option value="START">Start</option>
                <option value="STOP">Stop</option>
                <option value="RESTART">Restart</option>
                <option value="RESET">Reset</option>
                <option value="STATUS">Get Status</option>
                <option value="CONFIG">Update Config</option>
                <option value="CALIBRATE">Calibrate</option>
                <option value="DIAGNOSTIC">Run Diagnostic</option>
                <option value="CUSTOM">Custom Command</option>
              </select>
            </div>

            {/* Parameters */}
            <div>
              <label className="label">Parameters (JSON)</label>
              <textarea
                {...register('parameters')}
                rows={3}
                className="input font-mono text-sm"
                placeholder='{"key": "value", "threshold": 25}'
              />
              <p className="text-xs text-gray-500 mt-1">
                Enter parameters as valid JSON format
              </p>
            </div>
          </div>

          {/* Submit Button */}
          <div className="flex justify-end">
            <button
              type="submit"
              disabled={isExecuting || !onlineDevices.length}
              className="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isExecuting ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Sending...
                </>
              ) : (
                <>
                  <Send className="h-4 w-4 mr-2" />
                  Send Command
                </>
              )}
            </button>
          </div>
        </form>
      </div>

      {/* Quick Actions */}
      <div className="card p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <button
            onClick={() => {
              reset({
                commandType: 'broadcast',
                command: 'STATUS',
                parameters: '{}'
              });
            }}
            className="btn-outline flex flex-col items-center p-4"
          >
            <Target className="h-6 w-6 mb-2" />
            <span className="text-sm">Get All Status</span>
          </button>
          
          <button
            onClick={() => {
              reset({
                commandType: 'broadcast',
                command: 'RESTART',
                parameters: '{}'
              });
            }}
            className="btn-outline flex flex-col items-center p-4"
          >
            <RotateCcw className="h-6 w-6 mb-2" />
            <span className="text-sm">Restart All</span>
          </button>
          
          <button
            onClick={() => {
              reset({
                commandType: 'broadcast',
                command: 'DIAGNOSTIC',
                parameters: '{}'
              });
            }}
            className="btn-outline flex flex-col items-center p-4"
          >
            <Settings className="h-6 w-6 mb-2" />
            <span className="text-sm">Run Diagnostics</span>
          </button>
          
          <button
            onClick={() => {
              reset({
                commandType: 'broadcast',
                command: 'CALIBRATE',
                parameters: '{}'
              });
            }}
            className="btn-outline flex flex-col items-center p-4"
          >
            <Zap className="h-6 w-6 mb-2" />
            <span className="text-sm">Calibrate All</span>
          </button>
        </div>
      </div>

      {/* Command History */}
      <div className="card p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-gray-900">Command History</h3>
          <div className="flex items-center space-x-2">
            <span className="text-sm text-gray-500">
              {filteredHistory.length} commands
            </span>
          </div>
        </div>

        {/* Filters */}
        {showFilters && (
          <div className="mb-4 p-4 bg-gray-50 rounded-md">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
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
                <label className="label">Status</label>
                <select
                  value={filters.status}
                  onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                  className="input"
                >
                  <option value="">All Status</option>
                  <option value="SENT">Sent</option>
                  <option value="SUCCESS">Success</option>
                  <option value="FAILED">Failed</option>
                </select>
              </div>
              <div>
                <label className="label">Command</label>
                <select
                  value={filters.type}
                  onChange={(e) => setFilters({ ...filters, type: e.target.value })}
                  className="input"
                >
                  <option value="">All Commands</option>
                  <option value="START">Start</option>
                  <option value="STOP">Stop</option>
                  <option value="RESTART">Restart</option>
                  <option value="RESET">Reset</option>
                  <option value="STATUS">Status</option>
                  <option value="CONFIG">Config</option>
                  <option value="CALIBRATE">Calibrate</option>
                  <option value="DIAGNOSTIC">Diagnostic</option>
                </select>
              </div>
            </div>
          </div>
        )}

        {/* History Table */}
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Device
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Command
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Timestamp
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Response
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredHistory.length === 0 ? (
                <tr>
                  <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                    No commands sent yet
                  </td>
                </tr>
              ) : (
                filteredHistory.map((command) => (
                  <tr key={command.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {command.deviceName}
                      </div>
                      <div className="text-sm text-gray-500">
                        {command.deviceId}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{command.command}</div>
                      {command.parameters && (
                        <div className="text-xs text-gray-500 font-mono">
                          {command.parameters}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(command.status)}`}>
                        {getStatusIcon(command.status)}
                        <span className="ml-1">{command.status}</span>
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(command.timestamp).toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {command.response ? (
                        <span className="text-green-600">Success</span>
                      ) : (
                        <span className="text-gray-400">Pending</span>
                      )}
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

export default ControlCommands;
