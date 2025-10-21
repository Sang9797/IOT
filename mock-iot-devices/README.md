# Mock IoT Devices Simulator

A Python-based mock IoT device simulator that creates realistic IoT devices to communicate with the IoT Microservices System. This simulator is perfect for testing, development, and demonstration purposes.

## Features

- **Multiple Device Types**: Temperature sensors, pressure sensors, vibration monitors, and control valves
- **Realistic Data Generation**: Generates realistic sensor data with noise, drift, and anomalies
- **MQTT Communication**: Connects to MQTT broker and publishes sensor data
- **API Integration**: Registers devices with the API Gateway
- **Command Handling**: Responds to control commands from the system
- **Interactive Mode**: Command-line interface for device management
- **Anomaly Simulation**: Simulates various types of anomalies and failures
- **Multi-threaded**: Each device runs in its own thread for realistic behavior

## Device Types

### Temperature Sensor
- **Data**: Temperature (°C), Humidity (%)
- **Range**: -10°C to 80°C
- **Sampling Rate**: 5 seconds
- **Anomaly Threshold**: 70°C

### Pressure Sensor
- **Data**: Pressure (bar), Flow Rate (L/min)
- **Range**: 0.1 to 10.0 bar
- **Sampling Rate**: 10 seconds
- **Anomaly Threshold**: 8.0 bar

### Vibration Monitor
- **Data**: Vibration (g), Frequency (Hz)
- **Range**: 0.0 to 5.0 g
- **Sampling Rate**: 1 second
- **Anomaly Threshold**: 4.0 g

### Control Valve
- **Data**: Position (%), Flow Rate (L/min)
- **Range**: 0 to 100%
- **Sampling Rate**: 30 seconds
- **Anomaly Threshold**: 90%

## Installation

### Prerequisites
- Python 3.8 or higher
- pip (Python package installer)

### Setup
```bash
# Navigate to mock devices directory
cd mock-iot-devices

# Create virtual environment (recommended)
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Copy environment configuration
cp env.example .env

# Edit configuration if needed
nano .env
```

## Usage

### Basic Usage

#### Start with default settings (10 devices)
```bash
python main.py
```

#### Start with custom device count
```bash
python main.py --count 20
```

#### Start with specific factory ID
```bash
python main.py --factory-id factory-002
```

#### Start in interactive mode
```bash
python main.py --interactive
```

#### Start without API registration
```bash
python main.py --no-register
```

### Command Line Options

```bash
python main.py --help
```

Options:
- `--count N`: Number of devices to create (default: 10)
- `--factory-id ID`: Factory ID for devices (default: factory-001)
- `--no-register`: Skip device registration with API
- `--interactive`: Run in interactive mode
- `--log-level LEVEL`: Set log level (DEBUG, INFO, WARNING, ERROR)

### Interactive Mode

When running in interactive mode, you can:

1. **Show device status** - View all devices and their current state
2. **Send broadcast commands** - Send commands to all devices
3. **Send device-specific commands** - Send commands to individual devices
4. **Simulate anomalies** - Force anomaly conditions
5. **Simulate device failures** - Simulate device failures
6. **Restore devices** - Restore failed devices
7. **Get device reports** - Retrieve device reports from the system

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

mock-device-002:
  Type: PRESSURE_SENSOR
  Status: ONLINE
  Battery: 98.1%
  Data Count: 623
  Anomalies: 1
```

## Configuration

### Environment Variables

Create a `.env` file with the following variables:

```bash
# MQTT Configuration
MQTT_BROKER_HOST=localhost
MQTT_BROKER_PORT=1883
MQTT_USERNAME=
MQTT_PASSWORD=

# API Configuration
API_GATEWAY_URL=http://localhost:8080

# Device Configuration
DEFAULT_FACTORY_ID=factory-001
DEFAULT_LOCATION=Production Line A
DEVICE_COUNT=10

# Data Generation Settings
DATA_INTERVAL=5
ANOMALY_PROBABILITY=0.05
BATTERY_DRAIN_RATE=0.1

# Logging
LOG_LEVEL=INFO
```

### Device Configuration

Device types and their configurations are defined in `config.py`:

```python
DEVICE_TYPES = {
    'TEMPERATURE_SENSOR': {
        'base_temperature': 25.0,
        'temperature_range': (-10, 80),
        'anomaly_threshold': 70,
        'sampling_rate': 5
    },
    # ... other device types
}
```

## Data Format

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

### Status Update
```json
{
  "deviceId": "mock-device-001",
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": "ONLINE",
  "batteryLevel": 95.2,
  "signalStrength": -45,
  "lastSeen": "2024-01-15T10:30:00.000Z",
  "dataCounter": 1247,
  "anomalyCount": 3
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

## MQTT Topics

### Data Topics
- `devices/{deviceId}/data` - Sensor data
- `devices/{deviceId}/status` - Device status updates

### Control Topics
- `devices/{deviceId}/control` - Device-specific commands
- `devices/all/control` - Broadcast commands

## Testing

### Manual Testing

#### Test MQTT Connection
```bash
# Subscribe to device data
mosquitto_sub -h localhost -t "devices/+/data"

# Subscribe to device status
mosquitto_sub -h localhost -t "devices/+/status"
```

#### Test API Integration
```bash
# Check registered devices
curl http://localhost:8080/api/devices

# Send control command
curl -X POST http://localhost:8080/api/processor/devices/mock-device-001/control \
  -H "Content-Type: application/json" \
  -d '{"commandId":"test-001","commandType":"STATUS_CHECK","payload":"check_status"}'
```

### Automated Testing

```bash
# Run with debug logging
python main.py --log-level DEBUG

# Test with different device counts
python main.py --count 5
python main.py --count 50
```

## Troubleshooting

### Common Issues

#### MQTT Connection Failed
```
Error: Failed to connect device mock-device-001: [Errno 111] Connection refused
```
**Solution**: Ensure MQTT broker is running on the specified host and port.

#### API Registration Failed
```
Error: Failed to register device mock-device-001: 500 - Internal Server Error
```
**Solution**: Ensure the API Gateway and Device Management Service are running.

#### Device Not Responding
```
Warning: Device mock-device-001 disconnected from MQTT broker
```
**Solution**: Check network connectivity and MQTT broker status.

### Debug Mode

Run with debug logging to see detailed information:

```bash
python main.py --log-level DEBUG
```

### Log Files

Logs are written to both console and `mock_devices.log` file.

## Integration with IoT System

### Prerequisites
1. Start the IoT microservices system:
   ```bash
   cd ..
   ./scripts/start-services.sh
   ```

2. Ensure MQTT broker is running:
   ```bash
   docker-compose ps mosquitto
   ```

### Integration Steps

1. **Start Mock Devices**:
   ```bash
   python main.py --count 20 --interactive
   ```

2. **Verify Device Registration**:
   ```bash
   curl http://localhost:8080/api/devices | jq '.[] | select(.name | contains("Mock"))'
   ```

3. **Monitor Data Flow**:
   - Check InfluxDB for time series data
   - Monitor Kafka topics for message flow
   - Watch WebSocket notifications

4. **Test Control Commands**:
   - Use interactive mode to send commands
   - Verify device responses
   - Check system logs

## Performance Considerations

### Scaling
- **Small Scale**: 10-50 devices (single machine)
- **Medium Scale**: 50-500 devices (multiple machines)
- **Large Scale**: 500+ devices (distributed deployment)

### Resource Usage
- **Memory**: ~10MB per 100 devices
- **CPU**: Minimal impact
- **Network**: Depends on sampling rate and data size

### Optimization Tips
1. Adjust sampling rates based on needs
2. Use batch processing for large device counts
3. Implement connection pooling for API calls
4. Monitor system resources during testing

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is part of the IoT Microservices System and follows the same license terms.
