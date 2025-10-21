#!/bin/bash

# IoT System Test Script

echo "🧪 Testing IoT Microservices System..."

# Test API Gateway
echo "🔍 Testing API Gateway..."
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "✅ API Gateway is healthy"
else
    echo "❌ API Gateway health check failed"
fi

# Test Device Management Service
echo "🔍 Testing Device Management Service..."
if curl -s http://localhost:8081/actuator/health | grep -q "UP"; then
    echo "✅ Device Management Service is healthy"
else
    echo "❌ Device Management Service health check failed"
fi

# Test Device Processor Service
echo "🔍 Testing Device Processor Service..."
if curl -s http://localhost:8082/actuator/health | grep -q "UP"; then
    echo "✅ Device Processor Service is healthy"
else
    echo "❌ Device Processor Service health check failed"
fi

# Test Analysis Service
echo "🔍 Testing Analysis Service..."
if curl -s http://localhost:8083/actuator/health | grep -q "UP"; then
    echo "✅ Analysis Service is healthy"
else
    echo "❌ Analysis Service health check failed"
fi

# Test Notification Service
echo "🔍 Testing Notification Service..."
if curl -s http://localhost:8084/actuator/health | grep -q "UP"; then
    echo "✅ Notification Service is healthy"
else
    echo "❌ Notification Service health check failed"
fi

# Test device creation
echo "🔍 Testing device creation..."
device_response=$(curl -s -X POST http://localhost:8080/api/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Device",
    "address": "192.168.1.200",
    "type": "SENSOR",
    "status": "ONLINE",
    "factoryId": "test-factory",
    "location": "Test Location"
  }')

if echo "$device_response" | grep -q "id"; then
    echo "✅ Device creation test passed"
    device_id=$(echo "$device_response" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    echo "📱 Created device ID: $device_id"
else
    echo "❌ Device creation test failed"
    echo "Response: $device_response"
fi

# Test MQTT message
echo "🔍 Testing MQTT message..."
if command -v mosquitto_pub &> /dev/null; then
    mosquitto_pub -h localhost -t "devices/test-device/data" -m '{"temperature": 25.5, "pressure": 2.1, "vibration": 0.5}'
    echo "✅ MQTT test message sent"
else
    echo "⚠️ mosquitto_pub not found, skipping MQTT test"
fi

# Test WebSocket connection
echo "🔍 Testing WebSocket connection..."
if command -v wscat &> /dev/null; then
    echo "✅ WebSocket test available (wscat installed)"
else
    echo "⚠️ wscat not found, skipping WebSocket test"
fi

echo ""
echo "🎉 System testing completed!"
echo ""
echo "📊 Test Results Summary:"
echo "  - Check the output above for individual test results"
echo "  - All services should show ✅ for healthy status"
echo "  - Device creation should return a device ID"
echo ""
echo "🔧 Manual Testing:"
echo "  - Visit http://localhost:8080 for API Gateway"
echo "  - Visit http://localhost:8761 for Eureka Dashboard"
echo "  - Visit http://localhost:8086 for InfluxDB UI"
echo ""
