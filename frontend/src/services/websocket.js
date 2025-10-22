import { io } from 'socket.io-client';
import { toast } from 'react-hot-toast';

class WebSocketService {
  constructor() {
    this.socket = null;
    this.isConnected = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 5000;
    this.listeners = new Map();
  }

  connect(token) {
    if (this.socket && this.isConnected) {
      return;
    }

    // Connect to notification service WebSocket endpoint
    const wsUrl = process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws';
    
    this.socket = io(wsUrl, {
      transports: ['websocket'],
      auth: {
        token: token
      },
      reconnection: true,
      reconnectionAttempts: this.maxReconnectAttempts,
      reconnectionDelay: this.reconnectInterval,
    });

    this.setupEventListeners();
  }

  setupEventListeners() {
    if (!this.socket) return;

    this.socket.on('connect', () => {
      console.log('WebSocket connected');
      this.isConnected = true;
      this.reconnectAttempts = 0;
      this.emit('connection', { status: 'connected' });
      
      // Subscribe to notifications
      this.subscribeToNotifications();
    });

    this.socket.on('disconnect', (reason) => {
      console.log('WebSocket disconnected:', reason);
      this.isConnected = false;
      this.emit('connection', { status: 'disconnected', reason });
    });

    this.socket.on('connect_error', (error) => {
      console.error('WebSocket connection error:', error);
      this.isConnected = false;
      this.reconnectAttempts++;
      this.emit('connection', { status: 'error', error });
    });

    this.socket.on('reconnect', (attemptNumber) => {
      console.log('WebSocket reconnected after', attemptNumber, 'attempts');
      this.isConnected = true;
      this.reconnectAttempts = 0;
      this.emit('connection', { status: 'reconnected', attempts: attemptNumber });
    });

    this.socket.on('reconnect_error', (error) => {
      console.error('WebSocket reconnection error:', error);
      this.emit('connection', { status: 'reconnect_error', error });
    });

    this.socket.on('reconnect_failed', () => {
      console.error('WebSocket reconnection failed');
      this.emit('connection', { status: 'reconnect_failed' });
      toast.error('Connection lost. Please refresh the page.');
    });

    // IoT System specific events
    this.socket.on('notification', (data) => {
      console.log('Received notification:', data);
      this.emit('notification', data);
      
      // Show toast notification
      if (data.notification) {
        const notification = data.notification;
        toast(notification.message, {
          duration: 5000,
          icon: this.getNotificationIcon(notification.type),
          style: {
            background: this.getNotificationColor(notification.type),
            color: 'white',
          },
        });
      }
    });

    this.socket.on('alert', (data) => {
      console.log('Received alert:', data);
      this.emit('alert', data);
      
      // Show alert toast
      toast(`Alert: ${data.message}`, {
        duration: 8000,
        icon: '⚠️',
        style: {
          background: this.getAlertColor(data.severity),
          color: 'white',
        },
      });
    });

    this.socket.on('device_status', (data) => {
      console.log('Device status update:', data);
      this.emit('device_status', data);
    });

    this.socket.on('device_data', (data) => {
      console.log('Device data update:', data);
      this.emit('device_data', data);
    });

    this.socket.on('system_health', (data) => {
      console.log('System health update:', data);
      this.emit('system_health', data);
    });
  }

  subscribeToNotifications() {
    if (!this.socket || !this.isConnected) return;

    this.socket.emit('subscribe', {
      type: 'subscribe',
      subscriptionType: 'all'
    });
  }

  subscribeToDevice(deviceId) {
    if (!this.socket || !this.isConnected) return;

    this.socket.emit('subscribe', {
      type: 'subscribe',
      subscriptionType: 'device',
      deviceId: deviceId
    });
  }

  subscribeToFactory(factoryId) {
    if (!this.socket || !this.isConnected) return;

    this.socket.emit('subscribe', {
      type: 'subscribe',
      subscriptionType: 'factory',
      factoryId: factoryId
    });
  }

  unsubscribe() {
    if (!this.socket || !this.isConnected) return;

    this.socket.emit('unsubscribe', {
      type: 'unsubscribe'
    });
  }

  // Event listener management
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event).push(callback);
  }

  off(event, callback) {
    if (!this.listeners.has(event)) return;
    
    const callbacks = this.listeners.get(event);
    const index = callbacks.indexOf(callback);
    if (index > -1) {
      callbacks.splice(index, 1);
    }
  }

  emit(event, data) {
    if (!this.listeners.has(event)) return;
    
    this.listeners.get(event).forEach(callback => {
      try {
        callback(data);
      } catch (error) {
        console.error('Error in WebSocket event callback:', error);
      }
    });
  }

  // Utility methods
  getNotificationIcon(type) {
    const icons = {
      'EMAIL': '📧',
      'SMS': '📱',
      'DASHBOARD': '📊',
      'WEBHOOK': '🔗',
      'PUSH': '🔔',
    };
    return icons[type] || '📢';
  }

  getNotificationColor(type) {
    const colors = {
      'EMAIL': '#3b82f6',
      'SMS': '#10b981',
      'DASHBOARD': '#8b5cf6',
      'WEBHOOK': '#f59e0b',
      'PUSH': '#ef4444',
    };
    return colors[type] || '#6b7280';
  }

  getAlertColor(severity) {
    const colors = {
      'LOW': '#3b82f6',
      'MEDIUM': '#f59e0b',
      'HIGH': '#ef4444',
      'CRITICAL': '#dc2626',
    };
    return colors[severity] || '#6b7280';
  }

  // Connection status
  isSocketConnected() {
    return this.socket && this.isConnected;
  }

  getConnectionStatus() {
    return {
      connected: this.isConnected,
      socket: this.socket,
      reconnectAttempts: this.reconnectAttempts,
    };
  }

  // Disconnect
  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
      this.isConnected = false;
      this.listeners.clear();
    }
  }

  // Ping/Pong for connection testing
  ping() {
    if (!this.socket || !this.isConnected) return false;
    
    this.socket.emit('ping', { timestamp: Date.now() });
    return true;
  }
}

// Create singleton instance
const webSocketService = new WebSocketService();

export default webSocketService;
