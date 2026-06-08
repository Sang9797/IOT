import React, { useEffect, useState } from 'react';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { mqttBrokerAPI } from '../../services/api';
import MqttBrokerForm from '../../components/Brokers/MqttBrokerForm';

const MqttBrokerManagement = () => {
  const [brokers, setBrokers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingBroker, setEditingBroker] = useState(null);
  const [showForm, setShowForm] = useState(false);

  const loadBrokers = async () => {
    setLoading(true);
    try {
      const response = await mqttBrokerAPI.getBrokers();
      setBrokers(response.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBrokers();
  }, []);

  const handleSubmit = async (brokerData) => {
    if (editingBroker) {
      await mqttBrokerAPI.updateBroker(editingBroker.id, brokerData);
      toast.success('Broker updated successfully');
    } else {
      await mqttBrokerAPI.createBroker(brokerData);
      toast.success('Broker created successfully');
    }
    setShowForm(false);
    setEditingBroker(null);
    await loadBrokers();
  };

  const handleDelete = async (broker) => {
    if (!window.confirm(`Delete broker ${broker.name}?`)) {
      return;
    }
    try {
      await mqttBrokerAPI.deleteBroker(broker.id);
      toast.success('Broker deleted successfully');
      await loadBrokers();
    } catch (error) {
      if (error.response?.status === 409) {
        toast.error(error.response.data?.message || 'Broker still has assigned devices');
        return;
      }
      throw error;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">MQTT Brokers</h1>
          <p className="text-gray-600">Manage broker inventory and device routing targets.</p>
        </div>
        <button onClick={() => { setEditingBroker(null); setShowForm(true); }} className="btn-primary">
          <Plus className="h-4 w-4 mr-2" />
          Add Broker
        </button>
      </div>

      <div className="card overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Endpoint</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Topic Prefix</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {loading ? (
              <tr><td colSpan="5" className="px-6 py-12 text-center text-gray-500">Loading brokers...</td></tr>
            ) : brokers.length === 0 ? (
              <tr><td colSpan="5" className="px-6 py-12 text-center text-gray-500">No brokers configured</td></tr>
            ) : (
              brokers.map((broker) => (
                <tr key={broker.id}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">{broker.name}</div>
                    <div className="text-xs text-gray-500">{broker.description || 'No description'}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {broker.protocol}://{broker.host}:{broker.port}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={broker.enabled ? 'badge-success' : 'badge-gray'}>
                      {broker.enabled ? 'Enabled' : 'Disabled'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{broker.topicPrefix || '/'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex items-center space-x-2">
                      <button onClick={() => { setEditingBroker(broker); setShowForm(true); }} className="text-gray-600 hover:text-gray-900">
                        <Edit className="h-4 w-4" />
                      </button>
                      <button onClick={() => handleDelete(broker)} className="text-red-600 hover:text-red-900">
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

      <MqttBrokerForm
        isOpen={showForm}
        onClose={() => { setShowForm(false); setEditingBroker(null); }}
        onSubmit={handleSubmit}
        broker={editingBroker}
        title={editingBroker ? 'Edit MQTT Broker' : 'Create MQTT Broker'}
      />
    </div>
  );
};

export default MqttBrokerManagement;
