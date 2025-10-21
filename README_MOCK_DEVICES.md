# Mock IoT Devices Integration

This document describes the Mock IoT Devices simulator that has been added to your IoT Microservices System. The simulator creates realistic IoT devices that communicate with your system for testing and development purposes.

## 🎯 Overview

The Mock IoT Devices simulator is a Python-based application that:

- **Simulates Real IoT Devices**: Creates realistic temperature sensors, pressure sensors, vibration monitors, and control valves
- **Generates Realistic Data**: Produces sensor data with noise, drift, and anomalies
- **Communicates via MQTT**: Publishes data to MQTT topics that your system consumes
- **Responds to Commands**: Handles control commands from your IoT system
- **Integrates with APIs**: Registers devices with your API Gateway
- **Provides Interactive Control**: Command-line interface for device management

## 📁 Project Structure

```
mock-iot-devices/
├── main.py                 # Main entry point
├── device.py              # Individual device implementation
├── device_manager.py      # Device management and coordination
├── config.py              # Configuration settings
├── demo.py                # Demo script showing features
├── requirements.txt       # Python dependencies
├── env.example           # Environment configuration template
├── README.md             # Detailed documentation
└── scripts/
    ├── start_mock_devices.sh  # Startup script
    └── test_mock_devices.sh   # Test script
```

## 🚀 Quick Start

### 1. Prerequisites
- Python 3.8 or higher
- IoT Microservices System running (from main project)

### 2. Setup
```bash
# Navigate to mock devices directory
cd mock-iot-devices

# Create virtual environment
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Copy environment configuration
cp env.example .env
```

### 3. Start Mock Devices
```bash
# Start with default settings (10 devices)
python main.py

# Start with custom settings
python main.py --count 20 --factory-id factory-002 --interactive

# Or use the startup script
./scripts/start_mock_devices.sh --count 15 --interactive
```

## 🔧 Device Types

### Temperature Sensor
- **Data**: Temperature (°C), Humidity (%)
- **Range**: -10°C to 80°C
- **Sampling**: Every 5 seconds
- **Anomalies**: Temperature > 70°C

### Pressure Sensor
- **Data**: Pressure (bar), Flow Rate (L/min)
- **Range**: 0.1 to 10.0 bar
- **Sampling**: Every 10 seconds
- **Anomalies**: Pressure > 8.0 bar

### Vibration Monitor
- **Data**: Vibration (g), Frequency (Hz)
- **Range**: 0.0 to 5.0 g
- **Sampling**: Every 1 second
- **Anomalies**: Vibration > 4.0 g

### Control Valve
- **Data**: Position (%), Flow Rate (L/min)
- **Range**: 0 to 100%
- **Sampling**: Every 30 seconds
- **Anomalies**: Position > 90%

## 📡 Communication

### MQTT Topics
- `devices/{deviceId}/data` - Sensor data
- `devices/{deviceId}/status` - Device status
- `devices/{deviceId}/control` - Device commands
- `devices/all/control` - Broadcast commands

### API Integration
- Registers devices with API Gateway
- Sends control commands via REST API
- Retrieves device reports and analytics

## 🎮 Interactive Mode

When running in interactive mode, you can:

1. **View Device Status** - See all devices and their current state
2. **Send Commands** - Control individual devices or broadcast to all
3. **Simulate Anomalies** - Force anomaly conditions for testing
4. **Simulate Failures** - Test system response to device failures
5. **Get Reports** - Retrieve device analytics from the system

### Example Interactive Session
```
==================================================
Mock IoT Device Manager - Interactive Mode
==================================================
1. Show device status
2. Send broadcast command
3. Send command to specific device
4. Simulate anomaly
5. Simulate device failure
6. Restore device
7. Get device reports
8. Exit
--------------------------------------------------
Enter your choice (1-8): 1

Device Status:
Total devices: 10
Running devices: 10
Registered devices: 10

mock-device-001:
  Type: TEMPERATURE_SENSOR
  Status: ONLINE
  Battery: 95.2%
  Data Count: 1247
  Anomalies: 3
```

## 🧪 Testing

### Run Tests
```bash
# Test all components
./scripts/test_mock_devices.sh

# Run demo
python demo.py
```

### Manual Testing
```bash
# Monitor MQTT data
mosquitto_sub -h localhost -t "devices/+/data"

# Check registered devices
curl http://localhost:8080/api/devices | jq '.[] | select(.name | contains("Mock"))'

# Send control command
curl -X POST http://localhost:8080/api/processor/devices/mock-device-001/control \
  -H "Content-Type: application/json" \
  -d '{"commandId":"test-001","commandType":"STATUS_CHECK","payload":"check_status"}'
```

## 📊 Data Examples

### Sensor Data
```json
{
  "deviceId": "mock-device-001",
  "timestamp": "2024-01-15T10:30:00.000Z",
  "factoryId": "factory-001",
  "location": "Production Line A - Device 1",
  "messageType": "sensor_data",
  "batteryLevel": 95.2,
  "signalStrength": -45,
  "data": {
    "value": 25.5,
    "unit": "°C",
    "isAnomaly": false,
    "anomalyCount": 3,
    "temperature": 25.5,
    "humidity": 45.2
  }
}
```

### Control Command Response
```json
{
  "deviceId": "mock-device-001",
  "commandId": "cmd-001",
  "commandType": "START",
  "status": "success",
  "timestamp": "2024-01-15T10:30:00.000Z",
  "response": {
    "message": "Device started successfully"
  }
}
```

## 🔧 Configuration

### Environment Variables
```bash
# MQTT Configuration
MQTT_BROKER_HOST=localhost
MQTT_BROKER_PORT=1883

# API Configuration
API_GATEWAY_URL=http://localhost:8080

# Device Configuration
DEFAULT_FACTORY_ID=factory-001
DEVICE_COUNT=10

# Data Generation
ANOMALY_PROBABILITY=0.05  # 5% chance of anomaly
BATTERY_DRAIN_RATE=0.1   # Battery drain per hour
```

## 🚀 Integration with IoT System

### 1. Start IoT System
```bash
cd ..
./scripts/start-services.sh
```

### 2. Start Mock Devices
```bash
cd mock-iot-devices
python main.py --count 20 --interactive
```

### 3. Monitor Integration
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **InfluxDB UI**: http://localhost:8086
- **Device Management**: Check registered devices
- **Real-time Data**: Monitor WebSocket notifications

## 📈 Performance

### Scaling Capabilities
- **Small Scale**: 10-50 devices (single machine)
- **Medium Scale**: 50-500 devices (multiple machines)
- **Large Scale**: 500+ devices (distributed deployment)

### Resource Usage
- **Memory**: ~10MB per 100 devices
- **CPU**: Minimal impact
- **Network**: Depends on sampling rate

## 🛠️ Troubleshooting

### Common Issues

#### MQTT Connection Failed
```
Error: Failed to connect device mock-device-001: [Errno 111] Connection refused
```
**Solution**: Ensure MQTT broker is running (`docker-compose ps mosquitto`)

#### API Registration Failed
```
Error: Failed to register device mock-device-001: 500 - Internal Server Error
```
**Solution**: Ensure API Gateway and Device Management Service are running

#### Device Not Responding
```
Warning: Device mock-device-001 disconnected from MQTT broker
```
**Solution**: Check network connectivity and MQTT broker status

### Debug Mode
```bash
python main.py --log-level DEBUG
```

## 🎯 Use Cases

### Development & Testing
- **Unit Testing**: Test individual components
- **Integration Testing**: Test system interactions
- **Load Testing**: Test system under load
- **Anomaly Testing**: Test anomaly detection

### Demonstrations
- **System Demos**: Show system capabilities
- **Training**: Train users on system features
- **Proof of Concept**: Demonstrate system value

### Production Simulation
- **Pre-deployment Testing**: Test before real devices
- **Performance Validation**: Validate system performance
- **Failure Simulation**: Test failure scenarios

## 📚 Additional Resources

- **Detailed Documentation**: `mock-iot-devices/README.md`
- **API Documentation**: `API_DOCUMENTATION.md`
- **Deployment Guide**: `DEPLOYMENT_GUIDE.md`
- **Main Project README**: `README.md`

## 🤝 Contributing

The Mock IoT Devices simulator is part of the IoT Microservices System. To contribute:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

---

**The Mock IoT Devices simulator provides a comprehensive testing and development environment for your IoT Microservices System, enabling you to validate functionality, test scenarios, and demonstrate capabilities without requiring physical IoT devices.**
