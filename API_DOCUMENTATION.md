# IoT Microservices API Documentation

## Overview

This document provides comprehensive API documentation for the IoT Microservices System. All APIs are accessible through the API Gateway at `http://localhost:8080`.

## Authentication

The system uses JWT-based authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Base URL

```
http://localhost:8080/api
```

## Device Management API

### Create Device
**POST** `/devices`

Creates a new IoT device in the system.

**Request Body:**
```json
{
  "name": "Temperature Sensor 1",
  "address": "192.168.1.100",
  "type": "SENSOR",
  "status": "ONLINE",
  "factoryId": "factory-001",
  "location": "Production Line A",
  "configuration": {
    "sampling_rate": "5",
    "threshold_high": "80",
    "threshold_low": "-10"
  }
}
```

**Response:**
```json
{
  "id": "device-001",
  "name": "Temperature Sensor 1",
  "address": "192.168.1.100",
  "type": "SENSOR",
  "status": "ONLINE",
  "factoryId": "factory-001",
  "location": "Production Line A",
  "lastSeen": "2024-01-15T10:30:00",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Get All Devices
**GET** `/devices`

Retrieves all devices in the system.

**Query Parameters:**
- `factoryId` (optional): Filter by factory ID
- `status` (optional): Filter by device status
- `type` (optional): Filter by device type

**Response:**
```json
[
  {
    "id": "device-001",
    "name": "Temperature Sensor 1",
    "address": "192.168.1.100",
    "type": "SENSOR",
    "status": "ONLINE",
    "factoryId": "factory-001",
    "location": "Production Line A",
    "lastSeen": "2024-01-15T10:30:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

### Get Device by ID
**GET** `/devices/{id}`

Retrieves a specific device by its ID.

**Response:**
```json
{
  "id": "device-001",
  "name": "Temperature Sensor 1",
  "address": "192.168.1.100",
  "type": "SENSOR",
  "status": "ONLINE",
  "factoryId": "factory-001",
  "location": "Production Line A",
  "lastSeen": "2024-01-15T10:30:00",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Update Device
**PUT** `/devices/{id}`

Updates an existing device.

**Request Body:**
```json
{
  "name": "Updated Temperature Sensor 1",
  "status": "MAINTENANCE",
  "location": "Production Line B"
}
```

### Delete Device
**DELETE** `/devices/{id}`

Deletes a device from the system.

**Response:** `204 No Content`

### Update Device Status
**PATCH** `/devices/{id}/status?status=ONLINE`

Updates the status of a specific device.

### Get Device Statistics
**GET** `/devices/stats/count`

Retrieves device statistics.

**Query Parameters:**
- `factoryId` (optional): Filter by factory ID

**Response:**
```json
{
  "totalDevices": 150,
  "onlineDevices": 145,
  "offlineDevices": 5
}
```

## Device Control API

### Send Control Command to Device
**POST** `/processor/devices/{deviceId}/control`

Sends a control command to a specific device.

**Request Body:**
```json
{
  "commandId": "cmd-001",
  "commandType": "START",
  "payload": "start_operation",
  "parameters": {
    "speed": 100,
    "duration": 300
  },
  "priority": 1,
  "timeoutSeconds": 30
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Control command sent to device device-001",
  "commandId": "cmd-001"
}
```

### Send Broadcast Command
**POST** `/processor/devices/all/control`

Sends a control command to all devices.

**Request Body:**
```json
{
  "commandId": "cmd-002",
  "commandType": "EMERGENCY_STOP",
  "payload": "emergency_stop_all",
  "priority": 10
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Broadcast control command sent to all devices",
  "commandId": "cmd-002"
}
```

## Analysis & Reports API

### Get Device Report
**GET** `/analysis/devices/{deviceId}/report`

Generates a comprehensive report for a specific device.

**Query Parameters:**
- `hours` (optional): Time period in hours (default: 24)

**Response:**
```json
{
  "deviceId": "device-001",
  "reportPeriod": "24 hours",
  "generatedAt": "2024-01-15T10:30:00",
  "data": [
    {
      "time": "2024-01-15T09:00:00Z",
      "measurement": "device_data",
      "field": "temperature",
      "value": 25.5,
      "device_id": "device-001",
      "factory_id": "factory-001"
    }
  ]
}
```

### Get Factory Report
**GET** `/analysis/factories/{factoryId}/report`

Generates a report for all devices in a factory.

**Query Parameters:**
- `hours` (optional): Time period in hours (default: 24)

### Get Anomaly Report
**GET** `/analysis/anomalies/report`

Retrieves a report of all detected anomalies.

**Query Parameters:**
- `hours` (optional): Time period in hours (default: 24)

**Response:**
```json
{
  "reportType": "anomaly",
  "reportPeriod": "24 hours",
  "generatedAt": "2024-01-15T10:30:00",
  "anomalies": [
    {
      "deviceId": "device-001",
      "anomalyType": "TEMPERATURE_HIGH",
      "severity": "HIGH",
      "timestamp": "2024-01-15T09:30:00",
      "details": "Temperature exceeded threshold: 85.2°C"
    }
  ]
}
```

### Get Performance Report
**GET** `/analysis/devices/{deviceId}/performance`

Generates a performance report for a specific device.

**Query Parameters:**
- `hours` (optional): Time period in hours (default: 24)

**Response:**
```json
{
  "deviceId": "device-001",
  "reportType": "performance",
  "reportPeriod": "24 hours",
  "generatedAt": "2024-01-15T10:30:00",
  "metrics": {
    "temperature": {
      "mean": 25.5,
      "max": 30.2,
      "min": 20.1,
      "stdDev": 2.1
    },
    "pressure": {
      "mean": 2.1,
      "max": 2.5,
      "min": 1.8,
      "stdDev": 0.2
    }
  }
}
```

## Notification API

### Send Custom Notification
**POST** `/notifications`

Sends a custom notification to specified recipients.

**Request Body:**
```json
{
  "type": "EMAIL",
  "title": "Device Alert",
  "message": "Device device-001 is offline",
  "recipients": ["admin@factory.com", "manager@factory.com"],
  "metadata": {
    "deviceId": "device-001",
    "severity": "HIGH"
  }
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Notification sent successfully",
  "notificationId": "notif-001"
}
```

### Get Notification Statistics
**GET** `/notifications/stats`

Retrieves notification service statistics.

**Response:**
```json
{
  "activeWebSocketConnections": 5,
  "service": "notification-service",
  "timestamp": "2024-01-15T10:30:00"
}
```

## WebSocket API

### Connection
**WebSocket** `/ws/notifications`

Establishes a WebSocket connection for real-time notifications.

### Subscribe to Notifications
```json
{
  "type": "subscribe",
  "subscriptionType": "all",
  "deviceId": "device-001",
  "factoryId": "factory-001"
}
```

### Unsubscribe from Notifications
```json
{
  "type": "unsubscribe"
}
```

### Ping/Pong
```json
{
  "type": "ping"
}
```

**Response:**
```json
{
  "type": "pong",
  "timestamp": 1642248600000
}
```

### Notification Message
```json
{
  "type": "notification",
  "notification": {
    "notificationId": "notif-001",
    "type": "DASHBOARD",
    "title": "Device Alert",
    "message": "Temperature threshold exceeded",
    "timestamp": "2024-01-15T10:30:00",
    "deviceId": "device-001",
    "factoryId": "factory-001"
  },
  "timestamp": 1642248600000
}
```

### Alert Message
```json
{
  "type": "alert",
  "deviceId": "device-001",
  "message": "Temperature threshold exceeded: 85.2°C",
  "severity": "HIGH",
  "timestamp": 1642248600000
}
```

### Device Status Update
```json
{
  "type": "device_status",
  "deviceId": "device-001",
  "status": "OFFLINE",
  "timestamp": 1642248600000
}
```

## Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid request data",
  "status": 400,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 401 Unauthorized
```json
{
  "error": "Authorization header is missing",
  "status": 401,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 404 Not Found
```json
{
  "error": "Device not found with id: device-999",
  "status": 404,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error",
  "status": 500,
  "timestamp": "2024-01-15T10:30:00"
}
```

## Rate Limiting

The API Gateway implements rate limiting:
- **Default**: 100 requests per minute per IP
- **Authenticated users**: 1000 requests per minute
- **Admin users**: 5000 requests per minute

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642248660
```

## Data Models

### Device Types
- `SENSOR`: Data collection devices
- `ACTUATOR`: Control devices
- `CONTROLLER`: Process control devices
- `MONITOR`: Monitoring devices

### Device Status
- `ONLINE`: Device is active and responding
- `OFFLINE`: Device is not responding
- `MAINTENANCE`: Device is in maintenance mode
- `ERROR`: Device has encountered an error

### Command Types
- `START`: Start device operation
- `STOP`: Stop device operation
- `RESTART`: Restart device
- `CONFIGURE`: Update device configuration
- `STATUS_CHECK`: Request device status
- `EMERGENCY_STOP`: Emergency stop command
- `MAINTENANCE_MODE`: Enter maintenance mode

### Alert Types
- `TEMPERATURE_HIGH`: Temperature above threshold
- `TEMPERATURE_LOW`: Temperature below threshold
- `PRESSURE_HIGH`: Pressure above threshold
- `PRESSURE_LOW`: Pressure below threshold
- `VIBRATION_ANOMALY`: Unusual vibration detected
- `POWER_FAILURE`: Power supply issue
- `COMMUNICATION_LOST`: Device communication lost
- `MAINTENANCE_DUE`: Maintenance required
- `PERFORMANCE_DEGRADATION`: Performance issues detected
- `SECURITY_BREACH`: Security violation detected

### Severity Levels
- `LOW`: Minor issue, informational
- `MEDIUM`: Moderate issue, requires attention
- `HIGH`: Serious issue, immediate action required
- `CRITICAL`: Critical issue, emergency response needed

### Notification Types
- `EMAIL`: Email notification
- `SMS`: SMS notification
- `DASHBOARD`: Dashboard notification
- `WEBHOOK`: Webhook notification
- `PUSH`: Push notification
