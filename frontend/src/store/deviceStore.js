import { create } from 'zustand';
import { deviceAPI } from '../services/api';
import { toast } from 'react-hot-toast';

const useDeviceStore = create((set, get) => ({
  // State
  devices: [],
  selectedDevice: null,
  deviceStats: null,
  isLoading: false,
  error: null,
  filters: {
    factoryId: '',
    status: '',
    type: '',
    search: '',
  },
  pagination: {
    page: 1,
    limit: 10,
    total: 0,
  },

  // Actions
  fetchDevices: async (params = {}) => {
    set({ isLoading: true, error: null });
    
    try {
      const { filters, pagination } = get();
      const queryParams = {
        ...filters,
        page: pagination.page,
        limit: pagination.limit,
        ...params,
      };
      
      const response = await deviceAPI.getDevices(queryParams);
      const devices = response.data;
      
      set({
        devices,
        isLoading: false,
        error: null,
      });
      
      return { success: true, devices };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to fetch devices';
      set({
        devices: [],
        isLoading: false,
        error: errorMessage,
      });
      
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  },

  fetchDevice: async (id) => {
    set({ isLoading: true, error: null });
    
    try {
      const response = await deviceAPI.getDevice(id);
      const device = response.data;
      
      set({
        selectedDevice: device,
        isLoading: false,
        error: null,
      });
      
      return { success: true, device };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to fetch device';
      set({
        selectedDevice: null,
        isLoading: false,
        error: errorMessage,
      });
      
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  },

  createDevice: async (deviceData) => {
    set({ isLoading: true, error: null });
    
    try {
      const response = await deviceAPI.createDevice(deviceData);
      const device = response.data;
      
      const { devices } = get();
      set({
        devices: [device, ...devices],
        isLoading: false,
        error: null,
      });
      
      toast.success('Device created successfully!');
      return { success: true, device };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to create device';
      set({
        isLoading: false,
        error: errorMessage,
      });
      
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  },

  updateDevice: async (id, deviceData) => {
    set({ isLoading: true, error: null });
    
    try {
      const response = await deviceAPI.updateDevice(id, deviceData);
      const updatedDevice = response.data;
      
      const { devices } = get();
      const updatedDevices = devices.map(device =>
        device.id === id ? updatedDevice : device
      );
      
      set({
        devices: updatedDevices,
        selectedDevice: updatedDevice,
        isLoading: false,
        error: null,
      });
      
      toast.success('Device updated successfully!');
      return { success: true, device: updatedDevice };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to update device';
      set({
        isLoading: false,
        error: errorMessage,
      });
      
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  },

  deleteDevice: async (id) => {
    set({ isLoading: true, error: null });
    
    try {
      await deviceAPI.deleteDevice(id);
      
      const { devices } = get();
      const filteredDevices = devices.filter(device => device.id !== id);
      
      set({
        devices: filteredDevices,
        selectedDevice: null,
        isLoading: false,
        error: null,
      });
      
      toast.success('Device deleted successfully!');
      return { success: true };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to delete device';
      set({
        isLoading: false,
        error: errorMessage,
      });
      
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  },

  updateDeviceStatus: async (id, status) => {
    set({ isLoading: true, error: null });
    
    try {
      await deviceAPI.updateDeviceStatus(id, status);
      
      const { devices } = get();
      const updatedDevices = devices.map(device =>
        device.id === id ? { ...device, status } : device
      );
      
      set({
        devices: updatedDevices,
        selectedDevice: get().selectedDevice?.id === id 
          ? { ...get().selectedDevice, status }
          : get().selectedDevice,
        isLoading: false,
        error: null,
      });
      
      toast.success(`Device status updated to ${status}!`);
      return { success: true };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to update device status';
      set({
        isLoading: false,
        error: errorMessage,
      });
      
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  },

  fetchDeviceStats: async (factoryId) => {
    set({ isLoading: true, error: null });
    
    try {
      const response = await deviceAPI.getDeviceStats(factoryId);
      const stats = response.data;
      
      set({
        deviceStats: stats,
        isLoading: false,
        error: null,
      });
      
      return { success: true, stats };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to fetch device stats';
      set({
        deviceStats: null,
        isLoading: false,
        error: errorMessage,
      });
      
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  },

  fetchOfflineDevices: async (minutesThreshold = 30) => {
    set({ isLoading: true, error: null });
    
    try {
      const response = await deviceAPI.getOfflineDevices(minutesThreshold);
      const offlineDevices = response.data;
      
      set({
        isLoading: false,
        error: null,
      });
      
      return { success: true, devices: offlineDevices };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to fetch offline devices';
      set({
        isLoading: false,
        error: errorMessage,
      });
      
      toast.error(errorMessage);
      return { success: false, error: errorMessage };
    }
  },

  // Filter and search actions
  setFilters: (filters) => {
    set({ filters: { ...get().filters, ...filters } });
  },

  clearFilters: () => {
    set({
      filters: {
        factoryId: '',
        status: '',
        type: '',
        search: '',
      },
    });
  },

  setPagination: (pagination) => {
    set({ pagination: { ...get().pagination, ...pagination } });
  },

  // Real-time updates
  updateDeviceFromWebSocket: (deviceData) => {
    const { devices } = get();
    const updatedDevices = devices.map(device =>
      device.id === deviceData.deviceId
        ? { ...device, ...deviceData, lastSeen: new Date().toISOString() }
        : device
    );
    
    set({ devices: updatedDevices });
  },

  updateDeviceStatusFromWebSocket: (deviceId, status) => {
    const { devices } = get();
    const updatedDevices = devices.map(device =>
      device.id === deviceId ? { ...device, status } : device
    );
    
    set({
      devices: updatedDevices,
      selectedDevice: get().selectedDevice?.id === deviceId
        ? { ...get().selectedDevice, status }
        : get().selectedDevice,
    });
  },

  // Utility functions
  getDeviceById: (id) => {
    const { devices } = get();
    return devices.find(device => device.id === id);
  },

  getDevicesByFactory: (factoryId) => {
    const { devices } = get();
    return devices.filter(device => device.factoryId === factoryId);
  },

  getDevicesByStatus: (status) => {
    const { devices } = get();
    return devices.filter(device => device.status === status);
  },

  getDevicesByType: (type) => {
    const { devices } = get();
    return devices.filter(device => device.type === type);
  },

  getFilteredDevices: () => {
    const { devices, filters } = get();
    let filtered = devices;
    
    if (filters.factoryId) {
      filtered = filtered.filter(device => device.factoryId === filters.factoryId);
    }
    
    if (filters.status) {
      filtered = filtered.filter(device => device.status === filters.status);
    }
    
    if (filters.type) {
      filtered = filtered.filter(device => device.type === filters.type);
    }
    
    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(device =>
        device.name.toLowerCase().includes(searchLower) ||
        device.address.toLowerCase().includes(searchLower) ||
        device.location.toLowerCase().includes(searchLower)
      );
    }
    
    return filtered;
  },

  // Clear state
  clearSelectedDevice: () => set({ selectedDevice: null }),
  clearError: () => set({ error: null }),
  reset: () => set({
    devices: [],
    selectedDevice: null,
    deviceStats: null,
    isLoading: false,
    error: null,
    filters: {
      factoryId: '',
      status: '',
      type: '',
      search: '',
    },
    pagination: {
      page: 1,
      limit: 10,
      total: 0,
    },
  }),
}));

export default useDeviceStore;
