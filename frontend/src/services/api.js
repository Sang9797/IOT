import axios from 'axios';
import { toast } from 'react-hot-toast';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    } else if (error.response?.status >= 500) {
      toast.error('Server error. Please try again later.');
    } else if (error.response?.data?.message) {
      toast.error(error.response.data.message);
    } else if (error.message) {
      toast.error(error.message);
    }
    return Promise.reject(error);
  }
);

// Device Management API
export const deviceAPI = {
  // Get all devices
  getDevices: (params = {}) => api.get('/devices', { params }),
  
  // Get device by ID
  getDevice: (id) => api.get(`/devices/${id}`),
  
  // Create device
  createDevice: (deviceData) => api.post('/devices', deviceData),
  
  // Update device
  updateDevice: (id, deviceData) => api.put(`/devices/${id}`, deviceData),
  
  // Delete device
  deleteDevice: (id) => api.delete(`/devices/${id}`),
  
  // Update device status
  updateDeviceStatus: (id, status) => api.patch(`/devices/${id}/status?status=${status}`),
  
  // Get device statistics
  getDeviceStats: (factoryId) => api.get('/devices/stats/count', { 
    params: factoryId ? { factoryId } : {} 
  }),
  
  // Get offline devices
  getOfflineDevices: (minutesThreshold = 30) => 
    api.get('/devices/offline', { params: { minutesThreshold } }),
};

// Device Control API
export const controlAPI = {
  // Send command to specific device
  sendDeviceCommand: (deviceId, command) => 
    api.post(`/processor/devices/${deviceId}/control`, command),
  
  // Send broadcast command to all devices
  sendBroadcastCommand: (command) => 
    api.post('/processor/devices/all/control', command),
  
  // Get processor health
  getProcessorHealth: () => api.get('/processor/health'),
  
  // Get processor stats
  getProcessorStats: () => api.get('/processor/stats'),
};

// Analysis & Reports API
export const analysisAPI = {
  // Get device report
  getDeviceReport: (deviceId, hours = 24) => 
    api.get(`/analysis/devices/${deviceId}/report`, { 
      params: { hours } 
    }),
  
  // Get factory report
  getFactoryReport: (factoryId, hours = 24) => 
    api.get(`/analysis/factories/${factoryId}/report`, { 
      params: { hours } 
    }),
  
  // Get anomaly report
  getAnomalyReport: (hours = 24) => 
    api.get('/analysis/anomalies/report', { 
      params: { hours } 
    }),
  
  // Get performance report
  getPerformanceReport: (deviceId, hours = 24) => 
    api.get(`/analysis/devices/${deviceId}/performance`, { 
      params: { hours } 
    }),
};

// Notification API
export const notificationAPI = {
  // Send custom notification
  sendNotification: (notificationData) => 
    api.post('/notifications', notificationData),
  
  // Get notification statistics
  getNotificationStats: () => api.get('/notifications/stats'),
  
  // Get notification health
  getNotificationHealth: () => api.get('/notifications/health'),
};

// User Management API - Note: You'll need to implement these endpoints in your backend
export const userAPI = {
  // Get all users
  getUsers: (params = {}) => api.get('/users', { params }),
  
  // Get user by ID
  getUser: (id) => api.get(`/users/${id}`),
  
  // Create user
  createUser: (userData) => api.post('/users', userData),
  
  // Update user
  updateUser: (id, userData) => api.put(`/users/${id}`, userData),
  
  // Delete user
  deleteUser: (id) => api.delete(`/users/${id}`),
  
  // Update user permissions
  updateUserPermissions: (id, permissions) => 
    api.patch(`/users/${id}/permissions`, permissions),
  
  // Get user roles
  getRoles: () => api.get('/users/roles'),
  
  // Get user permissions
  getPermissions: () => api.get('/users/permissions'),
};

// Authentication API - Note: You'll need to implement these endpoints in your backend
export const authAPI = {
  // Login
  login: (credentials) => api.post('/auth/login', credentials),
  
  // Logout
  logout: () => api.post('/auth/logout'),
  
  // Register
  register: (userData) => api.post('/auth/register', userData),
  
  // Refresh token
  refreshToken: () => api.post('/auth/refresh'),
  
  // Get current user
  getCurrentUser: () => api.get('/auth/me'),
  
  // Change password
  changePassword: (passwordData) => api.post('/auth/change-password', passwordData),
  
  // Forgot password
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  
  // Reset password
  resetPassword: (token, passwordData) => 
    api.post(`/auth/reset-password/${token}`, passwordData),
};

// Health Check API
export const healthAPI = {
  // Get system health
  getSystemHealth: () => api.get('/health'),
  
  // Get service health
  getServiceHealth: (service) => api.get(`/health/${service}`),
};

// Utility functions
export const apiUtils = {
  // Handle API errors
  handleError: (error) => {
    console.error('API Error:', error);
    if (error.response) {
      return error.response.data?.message || 'An error occurred';
    } else if (error.request) {
      return 'Network error. Please check your connection.';
    } else {
      return 'An unexpected error occurred';
    }
  },
  
  // Format error message
  formatErrorMessage: (error) => {
    const message = apiUtils.handleError(error);
    return message;
  },
  
  // Check if error is network error
  isNetworkError: (error) => {
    return !error.response && error.request;
  },
  
  // Check if error is server error
  isServerError: (error) => {
    return error.response && error.response.status >= 500;
  },
  
  // Check if error is client error
  isClientError: (error) => {
    return error.response && error.response.status >= 400 && error.response.status < 500;
  },
};

export default api;
