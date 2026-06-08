import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { AlertCircle, Save, X } from 'lucide-react';

const MqttBrokerForm = ({ isOpen, onClose, onSubmit, broker, title }) => {
  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm({
    defaultValues: {
      name: '',
      host: '',
      port: 1883,
      protocol: 'tcp',
      username: '',
      password: '',
      enabled: true,
      description: '',
      topicPrefix: '',
    },
  });

  useEffect(() => {
    if (broker) {
      Object.entries({
        name: broker.name || '',
        host: broker.host || '',
        port: broker.port || 1883,
        protocol: broker.protocol || 'tcp',
        username: broker.username || '',
        password: '',
        enabled: broker.enabled ?? true,
        description: broker.description || '',
        topicPrefix: broker.topicPrefix || '',
      }).forEach(([key, value]) => setValue(key, value));
    } else {
      reset();
    }
  }, [broker, reset, setValue]);

  if (!isOpen) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={onClose}></div>
        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg leading-6 font-medium text-gray-900">{title}</h3>
              <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
                <X className="h-6 w-6" />
              </button>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div>
                <label htmlFor="broker-name" className="label">Broker Name *</label>
                <input id="broker-name" {...register('name', { required: 'Broker name is required' })} className="input" />
                {errors.name && <p className="mt-1 text-sm text-red-600 flex items-center"><AlertCircle className="h-4 w-4 mr-1" />{errors.name.message}</p>}
              </div>
              <div>
                <label htmlFor="broker-host" className="label">Host *</label>
                <input id="broker-host" {...register('host', { required: 'Broker host is required' })} className="input" placeholder="broker.example.com" />
                {errors.host && <p className="mt-1 text-sm text-red-600 flex items-center"><AlertCircle className="h-4 w-4 mr-1" />{errors.host.message}</p>}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label htmlFor="broker-port" className="label">Port *</label>
                  <input id="broker-port" type="number" {...register('port', { required: 'Broker port is required', min: 1, max: 65535 })} className="input" />
                </div>
                <div>
                  <label htmlFor="broker-protocol" className="label">Protocol *</label>
                  <select id="broker-protocol" {...register('protocol', { required: 'Broker protocol is required' })} className="input">
                    <option value="tcp">tcp</option>
                    <option value="ssl">ssl</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label htmlFor="broker-username" className="label">Username</label>
                  <input id="broker-username" {...register('username')} className="input" />
                </div>
                <div>
                  <label htmlFor="broker-password" className="label">Password</label>
                  <input id="broker-password" type="password" {...register('password')} className="input" />
                </div>
              </div>
              <div>
                <label htmlFor="broker-topic-prefix" className="label">Topic Prefix</label>
                <input id="broker-topic-prefix" {...register('topicPrefix')} className="input" placeholder="factory-a" />
              </div>
              <div>
                <label htmlFor="broker-description" className="label">Description</label>
                <textarea id="broker-description" {...register('description')} rows={3} className="input" />
              </div>
              <label className="flex items-center gap-2 text-sm text-gray-700">
                <input type="checkbox" {...register('enabled')} />
                Enabled
              </label>
            </form>
          </div>
          <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button type="submit" onClick={handleSubmit(onSubmit)} className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary-600 text-base font-medium text-white hover:bg-primary-700 sm:ml-3 sm:w-auto sm:text-sm">
              <Save className="h-4 w-4 mr-2" />
              {broker ? 'Update Broker' : 'Create Broker'}
            </button>
            <button type="button" onClick={onClose} className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MqttBrokerForm;
