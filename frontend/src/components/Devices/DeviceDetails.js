import React from 'react';
import { X, Edit, Power, PowerOff, MapPin, Calendar, Cpu, Wifi, WifiOff, AlertTriangle } from 'lucide-react';

const DeviceDetails = ({ isOpen, onClose, device }) => {
  if (!isOpen || !device) return null;

  const getStatusColor = (status) => {
    switch (status) {
      case 'ONLINE':
        return 'text-green-600 bg-green-100';
      case 'OFFLINE':
        return 'text-red-600 bg-red-100';
      case 'MAINTENANCE':
        return 'text-yellow-600 bg-yellow-100';
      case 'ERROR':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'SENSOR':
        return 'text-blue-600 bg-blue-100';
      case 'ACTUATOR':
        return 'text-green-600 bg-green-100';
      case 'CONTROLLER':
        return 'text-yellow-600 bg-yellow-100';
      case 'MONITOR':
        return 'text-purple-600 bg-purple-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        {/* Background overlay */}
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={onClose}></div>

        {/* Modal panel */}
        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-2xl sm:w-full">
          {/* Header */}
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg leading-6 font-medium text-gray-900">
                Device Details
              </h3>
              <button
                onClick={onClose}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            {/* Device Information */}
            <div className="space-y-6">
              {/* Basic Information */}
              <div>
                <h4 className="text-md font-medium text-gray-900 mb-3">Basic Information</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500">Device Name</label>
                    <p className="text-sm text-gray-900">{device.name}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Device ID</label>
                    <p className="text-sm text-gray-900 font-mono">{device.id}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Type</label>
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getTypeColor(device.type)}`}>
                      {device.type}
                    </span>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Status</label>
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(device.status)}`}>
                      {device.status === 'ONLINE' ? (
                        <Wifi className="h-3 w-3 mr-1" />
                      ) : (
                        <WifiOff className="h-3 w-3 mr-1" />
                      )}
                      {device.status}
                    </span>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Address</label>
                    <p className="text-sm text-gray-900 font-mono">{device.address}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Factory ID</label>
                    <p className="text-sm text-gray-900">{device.factoryId || 'N/A'}</p>
                  </div>
                </div>
              </div>

              {/* Location Information */}
              <div>
                <h4 className="text-md font-medium text-gray-900 mb-3 flex items-center">
                  <MapPin className="h-4 w-4 mr-2" />
                  Location Information
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500">Location</label>
                    <p className="text-sm text-gray-900">{device.location || 'Not specified'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Coordinates</label>
                    <p className="text-sm text-gray-900">
                      {device.latitude && device.longitude 
                        ? `${device.latitude}, ${device.longitude}`
                        : 'Not available'
                      }
                    </p>
                  </div>
                </div>
              </div>

              {/* Timestamps */}
              <div>
                <h4 className="text-md font-medium text-gray-900 mb-3 flex items-center">
                  <Calendar className="h-4 w-4 mr-2" />
                  Timestamps
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500">Created At</label>
                    <p className="text-sm text-gray-900">
                      {device.createdAt ? new Date(device.createdAt).toLocaleString() : 'N/A'}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Updated At</label>
                    <p className="text-sm text-gray-900">
                      {device.updatedAt ? new Date(device.updatedAt).toLocaleString() : 'N/A'}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Last Seen</label>
                    <p className="text-sm text-gray-900">
                      {device.lastSeen ? new Date(device.lastSeen).toLocaleString() : 'Never'}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Last Data</label>
                    <p className="text-sm text-gray-900">
                      {device.lastDataReceived ? new Date(device.lastDataReceived).toLocaleString() : 'Never'}
                    </p>
                  </div>
                </div>
              </div>

              {/* Device Metrics */}
              <div>
                <h4 className="text-md font-medium text-gray-900 mb-3 flex items-center">
                  <Cpu className="h-4 w-4 mr-2" />
                  Device Metrics
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500">Battery Level</label>
                    <p className="text-sm text-gray-900">{device.batteryLevel || 'N/A'}%</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Signal Strength</label>
                    <p className="text-sm text-gray-900">{device.signalStrength || 'N/A'} dBm</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">Data Count</label>
                    <p className="text-sm text-gray-900">{device.dataCount || 0}</p>
                  </div>
                </div>
              </div>

              {/* Description */}
              {device.description && (
                <div>
                  <h4 className="text-md font-medium text-gray-900 mb-3">Description</h4>
                  <p className="text-sm text-gray-700">{device.description}</p>
                </div>
              )}

              {/* Metadata */}
              {device.metadata && Object.keys(device.metadata).length > 0 && (
                <div>
                  <h4 className="text-md font-medium text-gray-900 mb-3">Metadata</h4>
                  <div className="bg-gray-50 rounded-md p-3">
                    <pre className="text-xs text-gray-700 overflow-x-auto">
                      {JSON.stringify(device.metadata, null, 2)}
                    </pre>
                  </div>
                </div>
              )}

              {/* Error Information */}
              {device.status === 'ERROR' && device.errorMessage && (
                <div>
                  <h4 className="text-md font-medium text-gray-900 mb-3 flex items-center text-red-600">
                    <AlertTriangle className="h-4 w-4 mr-2" />
                    Error Information
                  </h4>
                  <div className="bg-red-50 border border-red-200 rounded-md p-3">
                    <p className="text-sm text-red-800">{device.errorMessage}</p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Footer */}
          <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              onClick={onClose}
              className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary-600 text-base font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:ml-3 sm:w-auto sm:text-sm"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DeviceDetails;
