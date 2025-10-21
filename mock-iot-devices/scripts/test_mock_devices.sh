#!/bin/bash

# Mock IoT Devices Test Script

echo "🧪 Testing Mock IoT Devices..."

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed."
    exit 1
fi

# Activate virtual environment if it exists
if [ -d "venv" ]; then
    source venv/bin/activate
fi

# Test 1: Check dependencies
echo "🔍 Testing dependencies..."
python3 -c "
import paho.mqtt.client
import requests
import websocket
import dotenv
import schedule
import numpy
import faker
import colorama
print('✅ All dependencies are installed')
" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ Some dependencies are missing. Run: pip install -r requirements.txt"
    exit 1
fi

# Test 2: Check configuration
echo "🔍 Testing configuration..."
python3 -c "
from config import MQTT_BROKER_HOST, API_GATEWAY_URL, DEVICE_COUNT
print(f'✅ Configuration loaded: MQTT={MQTT_BROKER_HOST}, API={API_GATEWAY_URL}, Devices={DEVICE_COUNT}')
" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ Configuration error. Check config.py"
    exit 1
fi

# Test 3: Check MQTT connection
echo "🔍 Testing MQTT connection..."
python3 -c "
import paho.mqtt.client as mqtt
import time
import sys

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print('✅ MQTT connection successful')
        client.disconnect()
    else:
        print(f'❌ MQTT connection failed: {rc}')
        sys.exit(1)

client = mqtt.Client()
client.on_connect = on_connect

try:
    client.connect('localhost', 1883, 5)
    client.loop_start()
    time.sleep(2)
    client.loop_stop()
except Exception as e:
    print(f'❌ MQTT connection error: {e}')
    sys.exit(1)
" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ MQTT connection test failed"
    exit 1
fi

# Test 4: Check API connection
echo "🔍 Testing API connection..."
python3 -c "
import requests
try:
    response = requests.get('http://localhost:8080/actuator/health', timeout=5)
    if response.status_code == 200:
        print('✅ API connection successful')
    else:
        print(f'❌ API connection failed: {response.status_code}')
        exit(1)
except Exception as e:
    print(f'❌ API connection error: {e}')
    exit(1)
" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ API connection test failed"
    exit 1
fi

# Test 5: Test device creation
echo "🔍 Testing device creation..."
python3 -c "
from device import MockIoTDevice
device = MockIoTDevice('test-device-001', 'TEMPERATURE_SENSOR', 'test-factory', 'Test Location')
print('✅ Device creation successful')
print(f'   Device ID: {device.device_id}')
print(f'   Device Type: {device.device_type}')
print(f'   Factory ID: {device.factory_id}')
" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ Device creation test failed"
    exit 1
fi

# Test 6: Test data generation
echo "🔍 Testing data generation..."
python3 -c "
from device import MockIoTDevice
device = MockIoTDevice('test-device-002', 'TEMPERATURE_SENSOR', 'test-factory', 'Test Location')
device.start()
data = device.generate_sensor_data()
if data and 'data' in data:
    print('✅ Data generation successful')
    print(f'   Temperature: {data[\"data\"][\"temperature\"]}°C')
    print(f'   Battery: {data[\"batteryLevel\"]}%')
else:
    print('❌ Data generation failed')
    exit(1)
" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ Data generation test failed"
    exit 1
fi

# Test 7: Test device manager
echo "🔍 Testing device manager..."
python3 -c "
from device_manager import DeviceManager
manager = DeviceManager()
devices = manager.create_devices(count=2, factory_id='test-factory')
if len(devices) == 2:
    print('✅ Device manager test successful')
    print(f'   Created {len(devices)} devices')
else:
    print('❌ Device manager test failed')
    exit(1)
" 2>/dev/null

if [ $? -ne 0 ]; then
    echo "❌ Device manager test failed"
    exit 1
fi

echo ""
echo "🎉 All tests passed successfully!"
echo ""
echo "📊 Test Summary:"
echo "  ✅ Dependencies installed"
echo "  ✅ Configuration loaded"
echo "  ✅ MQTT connection working"
echo "  ✅ API connection working"
echo "  ✅ Device creation working"
echo "  ✅ Data generation working"
echo "  ✅ Device manager working"
echo ""
echo "🚀 Ready to start mock devices!"
echo "   Run: ./scripts/start_mock_devices.sh"
echo "   Or: python main.py --interactive"
