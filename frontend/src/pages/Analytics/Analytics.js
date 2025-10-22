import React, { useState, useEffect } from 'react';
import { 
  BarChart3, 
  TrendingUp, 
  TrendingDown, 
  Activity,
  Download,
  Calendar,
  Filter,
  RefreshCw
} from 'lucide-react';
import { 
  LineChart, 
  Line, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer, 
  BarChart, 
  Bar, 
  PieChart, 
  Pie, 
  Cell,
  AreaChart,
  Area
} from 'recharts';
import { analysisAPI } from '../../services/api';
import { useDeviceStore } from '../../store/deviceStore';
import { toast } from 'react-hot-toast';

const Analytics = () => {
  const { devices, fetchDevices } = useDeviceStore();
  const [isLoading, setIsLoading] = useState(false);
  const [selectedDevice, setSelectedDevice] = useState('');
  const [selectedFactory, setSelectedFactory] = useState('');
  const [timeRange, setTimeRange] = useState('24h');
  const [analyticsData, setAnalyticsData] = useState({
    deviceReport: null,
    factoryReport: null,
    anomalyReport: null,
    performanceReport: null,
  });

  useEffect(() => {
    fetchDevices();
    loadAnalyticsData();
  }, [fetchDevices, selectedDevice, selectedFactory, timeRange]);

  const loadAnalyticsData = async () => {
    setIsLoading(true);
    try {
      const hours = timeRange === '1h' ? 1 : timeRange === '6h' ? 6 : timeRange === '24h' ? 24 : 168;
      
      const promises = [];
      
      if (selectedDevice) {
        promises.push(
          analysisAPI.getDeviceReport(selectedDevice, hours),
          analysisAPI.getPerformanceReport(selectedDevice, hours)
        );
      }
      
      if (selectedFactory) {
        promises.push(analysisAPI.getFactoryReport(selectedFactory, hours));
      }
      
      promises.push(analysisAPI.getAnomalyReport(hours));
      
      const results = await Promise.allSettled(promises);
      
      const newData = { ...analyticsData };
      let resultIndex = 0;
      
      if (selectedDevice) {
        if (results[resultIndex].status === 'fulfilled') {
          newData.deviceReport = results[resultIndex].value.data;
        }
        resultIndex++;
        
        if (results[resultIndex].status === 'fulfilled') {
          newData.performanceReport = results[resultIndex].value.data;
        }
        resultIndex++;
      }
      
      if (selectedFactory) {
        if (results[resultIndex].status === 'fulfilled') {
          newData.factoryReport = results[resultIndex].value.data;
        }
        resultIndex++;
      }
      
      if (results[resultIndex].status === 'fulfilled') {
        newData.anomalyReport = results[resultIndex].value.data;
      }
      
      setAnalyticsData(newData);
    } catch (error) {
      console.error('Error loading analytics data:', error);
      toast.error('Failed to load analytics data');
    } finally {
      setIsLoading(false);
    }
  };

  // Mock data for demonstration (replace with real data from API)
  const mockTimeSeriesData = [
    { time: '00:00', temperature: 25, pressure: 2.1, vibration: 0.5, humidity: 45 },
    { time: '04:00', temperature: 24, pressure: 2.0, vibration: 0.4, humidity: 47 },
    { time: '08:00', temperature: 26, pressure: 2.2, vibration: 0.6, humidity: 43 },
    { time: '12:00', temperature: 28, pressure: 2.3, vibration: 0.7, humidity: 41 },
    { time: '16:00', temperature: 27, pressure: 2.2, vibration: 0.5, humidity: 44 },
    { time: '20:00', temperature: 25, pressure: 2.1, vibration: 0.4, humidity: 46 },
  ];

  const mockAnomalyData = [
    { time: '00:00', anomalies: 0, normal: 100 },
    { time: '04:00', anomalies: 2, normal: 98 },
    { time: '08:00', anomalies: 1, normal: 99 },
    { time: '12:00', anomalies: 5, normal: 95 },
    { time: '16:00', anomalies: 3, normal: 97 },
    { time: '20:00', anomalies: 1, normal: 99 },
  ];

  const mockDevicePerformance = [
    { device: 'Sensor-001', efficiency: 95, uptime: 98, errors: 2 },
    { device: 'Sensor-002', efficiency: 87, uptime: 92, errors: 8 },
    { device: 'Sensor-003', efficiency: 92, uptime: 95, errors: 5 },
    { device: 'Actuator-001', efficiency: 88, uptime: 90, errors: 10 },
    { device: 'Controller-001', efficiency: 96, uptime: 99, errors: 1 },
  ];

  const mockFactoryData = [
    { factory: 'Factory A', devices: 45, online: 42, efficiency: 93 },
    { factory: 'Factory B', devices: 38, online: 35, efficiency: 89 },
    { factory: 'Factory C', devices: 52, online: 48, efficiency: 92 },
  ];

  const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

  const getTimeRangeLabel = () => {
    switch (timeRange) {
      case '1h': return 'Last Hour';
      case '6h': return 'Last 6 Hours';
      case '24h': return 'Last 24 Hours';
      case '7d': return 'Last 7 Days';
      default: return 'Last 24 Hours';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Analytics & Reports</h1>
          <p className="text-gray-600">Comprehensive analytics and performance insights</p>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={loadAnalyticsData}
            disabled={isLoading}
            className="btn-outline"
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
          <button className="btn-outline">
            <Download className="h-4 w-4 mr-2" />
            Export Report
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="card p-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="label">Time Range</label>
            <select
              value={timeRange}
              onChange={(e) => setTimeRange(e.target.value)}
              className="input"
            >
              <option value="1h">Last Hour</option>
              <option value="6h">Last 6 Hours</option>
              <option value="24h">Last 24 Hours</option>
              <option value="7d">Last 7 Days</option>
            </select>
          </div>
          <div>
            <label className="label">Device</label>
            <select
              value={selectedDevice}
              onChange={(e) => setSelectedDevice(e.target.value)}
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
            <label className="label">Factory</label>
            <select
              value={selectedFactory}
              onChange={(e) => setSelectedFactory(e.target.value)}
              className="input"
            >
              <option value="">All Factories</option>
              <option value="factory-001">Factory 001</option>
              <option value="factory-002">Factory 002</option>
              <option value="factory-003">Factory 003</option>
            </select>
          </div>
          <div className="flex items-end">
            <button
              onClick={loadAnalyticsData}
              className="btn-primary w-full"
            >
              <Filter className="h-4 w-4 mr-2" />
              Apply Filters
            </button>
          </div>
        </div>
      </div>

      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-blue-100 rounded-lg">
              <Activity className="h-6 w-6 text-blue-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Devices</p>
              <p className="text-2xl font-bold text-gray-900">{devices.length}</p>
              <p className="text-xs text-green-600 flex items-center">
                <TrendingUp className="h-3 w-3 mr-1" />
                +5% from last period
              </p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-green-100 rounded-lg">
              <TrendingUp className="h-6 w-6 text-green-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">System Efficiency</p>
              <p className="text-2xl font-bold text-gray-900">94.2%</p>
              <p className="text-xs text-green-600 flex items-center">
                <TrendingUp className="h-3 w-3 mr-1" />
                +2.1% from last period
              </p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-yellow-100 rounded-lg">
              <BarChart3 className="h-6 w-6 text-yellow-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Anomalies Detected</p>
              <p className="text-2xl font-bold text-gray-900">12</p>
              <p className="text-xs text-red-600 flex items-center">
                <TrendingDown className="h-3 w-3 mr-1" />
                -3 from last period
              </p>
            </div>
          </div>
        </div>

        <div className="card p-6">
          <div className="flex items-center">
            <div className="p-2 bg-purple-100 rounded-lg">
              <Calendar className="h-6 w-6 text-purple-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Uptime</p>
              <p className="text-2xl font-bold text-gray-900">99.1%</p>
              <p className="text-xs text-green-600 flex items-center">
                <TrendingUp className="h-3 w-3 mr-1" />
                +0.3% from last period
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Time Series Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Sensor Data Trend */}
        <div className="card p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Sensor Data Trend - {getTimeRangeLabel()}</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={mockTimeSeriesData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
              <Line type="monotone" dataKey="temperature" stroke="#ef4444" strokeWidth={2} />
              <Line type="monotone" dataKey="pressure" stroke="#3b82f6" strokeWidth={2} />
              <Line type="monotone" dataKey="vibration" stroke="#f59e0b" strokeWidth={2} />
              <Line type="monotone" dataKey="humidity" stroke="#10b981" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Anomaly Detection */}
        <div className="card p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Anomaly Detection - {getTimeRangeLabel()}</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={mockAnomalyData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
              <Area type="monotone" dataKey="anomalies" stackId="1" stroke="#ef4444" fill="#ef4444" />
              <Area type="monotone" dataKey="normal" stackId="1" stroke="#10b981" fill="#10b981" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Performance Analytics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Device Performance */}
        <div className="card p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Device Performance</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={mockDevicePerformance} layout="horizontal">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" domain={[0, 100]} />
              <YAxis dataKey="device" type="category" width={100} />
              <Tooltip />
              <Bar dataKey="efficiency" fill="#3b82f6" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Factory Distribution */}
        <div className="card p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Factory Distribution</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={mockFactoryData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ factory, devices }) => `${factory}: ${devices}`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="devices"
              >
                {mockFactoryData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Detailed Reports */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Device Report */}
        {selectedDevice && analyticsData.deviceReport && (
          <div className="card p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Device Report</h3>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-gray-600">Data Points</p>
                  <p className="text-lg font-semibold">{analyticsData.deviceReport.dataPoints || 0}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Average Value</p>
                  <p className="text-lg font-semibold">{analyticsData.deviceReport.averageValue || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Min Value</p>
                  <p className="text-lg font-semibold">{analyticsData.deviceReport.minValue || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Max Value</p>
                  <p className="text-lg font-semibold">{analyticsData.deviceReport.maxValue || 'N/A'}</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Anomaly Report */}
        {analyticsData.anomalyReport && (
          <div className="card p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Anomaly Report</h3>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-gray-600">Total Anomalies</p>
                  <p className="text-lg font-semibold">{analyticsData.anomalyReport.totalAnomalies || 0}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Anomaly Rate</p>
                  <p className="text-lg font-semibold">{analyticsData.anomalyReport.anomalyRate || '0%'}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Critical Anomalies</p>
                  <p className="text-lg font-semibold text-red-600">{analyticsData.anomalyReport.criticalAnomalies || 0}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Resolved</p>
                  <p className="text-lg font-semibold text-green-600">{analyticsData.anomalyReport.resolved || 0}</p>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 flex items-center space-x-3">
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary-600"></div>
            <span className="text-gray-700">Loading analytics data...</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default Analytics;
