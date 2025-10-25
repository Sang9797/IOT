import React, { useState, useEffect } from 'react';
import { 
  Activity, 
  Thermometer, 
  Gauge, 
  Zap, 
  Wifi, 
  WifiOff,
  Battery,
  Signal,
  AlertTriangle,
  TrendingUp,
  TrendingDown,
  Cpu
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from 'recharts';
import useDeviceStore from '../../store/deviceStore';
import webSocketService from '../../services/websocket';

const MonitoringDashboard = () => {
  const { devices, fetchDevices, fetchDeviceStats } = useDeviceStore();
  const [realTimeData, setRealTimeData] = useState([]);
  const [selectedDevice, setSelectedDevice] = useState(null);
  const [timeRange, setTimeRange] = useState('1h');
  const [isConnected, setIsConnected] = useState(false);

  // Real-time data from WebSocket
  const [chartData, setChartData] = useState([]);

  useEffect(() => {
    // Fetch initial data
    fetchDevices();
    fetchDeviceStats();
    
    // Set up WebSocket connection
    const token = localStorage.getItem('authToken');
    if (token) {
      webSocketService.connect(token);
    }

    // WebSocket event listeners
    const handleConnection = (data) => {
      setIsConnected(data.status === 'connected' || data.status === 'reconnected');
    };

    const handleDeviceData = (data) => {
      // Update real-time data when new data arrives from WebSocket
      setChartData(prev => {
        const newData = [...prev];
        // Keep only last 60 data points
        if (newData.length >= 60) {
          newData.shift();
        }
        
        // Extract sensor data from the WebSocket message
        const sensorData = data.data || {};
        newData.push({
          time: new Date().toLocaleTimeString(),
          timestamp: Date.now(),
          temperature: sensorData.temperature || sensorData.value || 0,
          pressure: sensorData.pressure || 0,
          vibration: sensorData.vibration || 0,
          humidity: sensorData.humidity || 0,
          deviceId: data.deviceId,
        });
        return newData;
      });
    };

    const handleAlert = (data) => {
      // Handle real-time alerts
      console.log('Alert received:', data);
    };

    const handleDeviceStatus = (data) => {
      // Handle device status updates
      console.log('Device status update:', data);
    };

    webSocketService.on('connection', handleConnection);
    webSocketService.on('device_data', handleDeviceData);
    webSocketService.on('alert', handleAlert);
    webSocketService.on('device_status', handleDeviceStatus);

    return () => {
      webSocketService.off('connection', handleConnection);
      webSocketService.off('device_data', handleDeviceData);
      webSocketService.off('alert', handleAlert);
      webSocketService.off('device_status', handleDeviceStatus);
    };
  }, [fetchDevices, fetchDeviceStats]);

  // Calculate statistics
  const onlineDevices = devices.filter(d => d.status === 'ONLINE').length;
  const offlineDevices = devices.filter(d => d.status === 'OFFLINE').length;
  const totalDevices = devices.length;
  const onlinePercentage = totalDevices > 0 ? (onlineDevices / totalDevices) * 100 : 0;

  // Device type distribution
  const deviceTypeData = devices.reduce((acc, device) => {
    acc[device.type] = (acc[device.type] || 0) + 1;
    return acc;
  }, {});

  const pieData = Object.entries(deviceTypeData).map(([type, count]) => ({
    name: type.replace('_', ' '),
    value: count,
  }));

  const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

  // Factory distribution
  const factoryData = devices.reduce((acc, device) => {
    const factory = device.factoryId || 'Unknown';
    acc[factory] = (acc[factory] || 0) + 1;
    return acc;
  }, {});

  const barData = Object.entries(factoryData).map(([factory, count]) => ({
    factory: factory.replace('factory-', 'Factory '),
    devices: count,
  }));

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Monitoring Dashboard</h1>
          <p className="text-gray-600">Real-time IoT device monitoring and analytics</p>
        </div>
        <div className="flex items-center space-x-4">
          <div className={`flex items-center px-3 py-1 rounded-full text-sm ${
            isConnected ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
          }`}>
            <div className={`w-2 h-2 rounded-full mr-2 ${
              isConnected ? 'bg-green-500 animate-pulse' : 'bg-red-500'
            }`} />
            {isConnected ? 'Connected' : 'Disconnected'}
          </div>
          <select
            value={timeRange}
            onChange={(e) => setTimeRange(e.target.value)}
            className="input w-32"
          >
            <option value="1h">Last Hour</option>
            <option value="6h">Last 6 Hours</option>
            <option value="24h">Last 24 Hours</option>
            <option value="7d">Last 7 Days</option>
          </select>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-blue-100 rounded-lg">
              <Activity className="h-6 w-6 text-blue-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Devices</p>
              <p className="text-2xl font-bold text-gray-900">{totalDevices}</p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-green-100 rounded-lg">
              <Wifi className="h-6 w-6 text-green-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Online Devices</p>
              <p className="text-2xl font-bold text-gray-900">{onlineDevices}</p>
              <p className="text-xs text-gray-500">{onlinePercentage.toFixed(1)}% online</p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-red-100 rounded-lg">
              <WifiOff className="h-6 w-6 text-red-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Offline Devices</p>
              <p className="text-2xl font-bold text-gray-900">{offlineDevices}</p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-yellow-100 rounded-lg">
              <AlertTriangle className="h-6 w-6 text-yellow-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Active Alerts</p>
              <p className="text-2xl font-bold text-gray-900">3</p>
            </div>
          </div>
        </div>
      </div>

      {/* Real-time Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Temperature Chart */}
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900 flex items-center">
              <Thermometer className="h-5 w-5 text-red-500 mr-2" />
              Temperature
            </h3>
            <div className="flex items-center text-sm text-gray-500">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-2 animate-pulse" />
              Real-time
            </div>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
              <Line 
                type="monotone" 
                dataKey="temperature" 
                stroke="#ef4444" 
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Pressure Chart */}
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900 flex items-center">
              <Gauge className="h-5 w-5 text-blue-500 mr-2" />
              Pressure
            </h3>
            <div className="flex items-center text-sm text-gray-500">
              <div className="w-2 h-2 bg-blue-500 rounded-full mr-2 animate-pulse" />
              Real-time
            </div>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
              <Line 
                type="monotone" 
                dataKey="pressure" 
                stroke="#3b82f6" 
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Vibration Chart */}
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900 flex items-center">
              <Zap className="h-5 w-5 text-yellow-500 mr-2" />
              Vibration
            </h3>
            <div className="flex items-center text-sm text-gray-500">
              <div className="w-2 h-2 bg-yellow-500 rounded-full mr-2 animate-pulse" />
              Real-time
            </div>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
              <Line 
                type="monotone" 
                dataKey="vibration" 
                stroke="#f59e0b" 
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Humidity Chart */}
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900 flex items-center">
              <Battery className="h-5 w-5 text-green-500 mr-2" />
              Humidity
            </h3>
            <div className="flex items-center text-sm text-gray-500">
              <div className="w-2 h-2 bg-green-500 rounded-full mr-2 animate-pulse" />
              Real-time
            </div>
          </div>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
              <Line 
                type="monotone" 
                dataKey="humidity" 
                stroke="#10b981" 
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Device Distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Device Types */}
        <div className="card p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Device Types</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {pieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Factory Distribution */}
        <div className="card p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Factory Distribution</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={barData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="factory" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="devices" fill="#3b82f6" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Recent Device Activity */}
      <div className="card p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Recent Device Activity</h3>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
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
                  Last Seen
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Battery
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Signal
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {devices.slice(0, 10).map((device) => (
                <tr key={device.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="flex-shrink-0 h-8 w-8">
                        <div className="h-8 w-8 rounded-full bg-gray-200 flex items-center justify-center">
                          <Cpu className="h-4 w-4 text-gray-600" />
                        </div>
                      </div>
                      <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900">{device.name}</div>
                        <div className="text-sm text-gray-500">{device.address}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="badge-gray">{device.type}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`${
                      device.status === 'ONLINE' ? 'status-online' :
                      device.status === 'OFFLINE' ? 'status-offline' :
                      device.status === 'MAINTENANCE' ? 'status-maintenance' :
                      'status-error'
                    }`}>
                      {device.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {device.lastSeen ? new Date(device.lastSeen).toLocaleString() : 'Never'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <Battery className="h-4 w-4 text-gray-400 mr-1" />
                      <span className="text-sm text-gray-900">
                        {device.batteryLevel || 'N/A'}%
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <Signal className="h-4 w-4 text-gray-400 mr-1" />
                      <span className="text-sm text-gray-900">
                        {device.signalStrength || 'N/A'} dBm
                      </span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default MonitoringDashboard;
