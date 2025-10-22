import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { X, Save, AlertCircle } from 'lucide-react';

const DeviceForm = ({ isOpen, onClose, onSubmit, device, title }) => {
  const { register, handleSubmit, formState: { errors }, reset, setValue } = useForm({
    defaultValues: {
      name: '',
      type: 'SENSOR',
      address: '',
      location: '',
      factoryId: '',
      status: 'ONLINE',
      description: '',
      metadata: {}
    }
  });

  useEffect(() => {
    if (device) {
      // Populate form with device data for editing
      setValue('name', device.name || '');
      setValue('type', device.type || 'SENSOR');
      setValue('address', device.address || '');
      setValue('location', device.location || '');
      setValue('factoryId', device.factoryId || '');
      setValue('status', device.status || 'ONLINE');
      setValue('description', device.description || '');
      setValue('metadata', device.metadata || {});
    } else {
      // Reset form for new device
      reset();
    }
  }, [device, setValue, reset]);

  const handleFormSubmit = (data) => {
    onSubmit(data);
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        {/* Background overlay */}
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={handleClose}></div>

        {/* Modal panel */}
        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          {/* Header */}
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg leading-6 font-medium text-gray-900">
                {title}
              </h3>
              <button
                onClick={handleClose}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-6 w-6" />
              </button>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
              {/* Device Name */}
              <div>
                <label className="label">Device Name *</label>
                <input
                  {...register('name', { required: 'Device name is required' })}
                  type="text"
                  className={`input ${errors.name ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                  placeholder="Enter device name"
                />
                {errors.name && (
                  <p className="mt-1 text-sm text-red-600 flex items-center">
                    <AlertCircle className="h-4 w-4 mr-1" />
                    {errors.name.message}
                  </p>
                )}
              </div>

              {/* Device Type */}
              <div>
                <label className="label">Device Type *</label>
                <select
                  {...register('type', { required: 'Device type is required' })}
                  className={`input ${errors.type ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                >
                  <option value="SENSOR">Sensor</option>
                  <option value="ACTUATOR">Actuator</option>
                  <option value="CONTROLLER">Controller</option>
                  <option value="MONITOR">Monitor</option>
                </select>
                {errors.type && (
                  <p className="mt-1 text-sm text-red-600 flex items-center">
                    <AlertCircle className="h-4 w-4 mr-1" />
                    {errors.type.message}
                  </p>
                )}
              </div>

              {/* Device Address */}
              <div>
                <label className="label">Device Address *</label>
                <input
                  {...register('address', { required: 'Device address is required' })}
                  type="text"
                  className={`input ${errors.address ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                  placeholder="192.168.1.100"
                />
                {errors.address && (
                  <p className="mt-1 text-sm text-red-600 flex items-center">
                    <AlertCircle className="h-4 w-4 mr-1" />
                    {errors.address.message}
                  </p>
                )}
              </div>

              {/* Location */}
              <div>
                <label className="label">Location</label>
                <input
                  {...register('location')}
                  type="text"
                  className="input"
                  placeholder="Building A, Floor 2, Room 201"
                />
              </div>

              {/* Factory ID */}
              <div>
                <label className="label">Factory ID</label>
                <select
                  {...register('factoryId')}
                  className="input"
                >
                  <option value="">Select Factory</option>
                  <option value="factory-001">Factory 001</option>
                  <option value="factory-002">Factory 002</option>
                  <option value="factory-003">Factory 003</option>
                </select>
              </div>

              {/* Status */}
              <div>
                <label className="label">Status</label>
                <select
                  {...register('status')}
                  className="input"
                >
                  <option value="ONLINE">Online</option>
                  <option value="OFFLINE">Offline</option>
                  <option value="MAINTENANCE">Maintenance</option>
                  <option value="ERROR">Error</option>
                </select>
              </div>

              {/* Description */}
              <div>
                <label className="label">Description</label>
                <textarea
                  {...register('description')}
                  rows={3}
                  className="input"
                  placeholder="Device description..."
                />
              </div>

              {/* Metadata */}
              <div>
                <label className="label">Metadata (JSON)</label>
                <textarea
                  {...register('metadata')}
                  rows={3}
                  className="input font-mono text-sm"
                  placeholder='{"model": "Sensor-X1", "version": "1.0", "manufacturer": "IoT Corp"}'
                />
                <p className="mt-1 text-xs text-gray-500">
                  Enter metadata as valid JSON format
                </p>
              </div>
            </form>
          </div>

          {/* Footer */}
          <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              type="submit"
              onClick={handleSubmit(handleFormSubmit)}
              className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary-600 text-base font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:ml-3 sm:w-auto sm:text-sm"
            >
              <Save className="h-4 w-4 mr-2" />
              {device ? 'Update Device' : 'Create Device'}
            </button>
            <button
              type="button"
              onClick={handleClose}
              className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DeviceForm;
