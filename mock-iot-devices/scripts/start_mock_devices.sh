#!/bin/bash

# Mock IoT Devices Startup Script

echo "🚀 Starting Mock IoT Devices Simulator..."

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.8 or higher."
    exit 1
fi

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "📦 Creating virtual environment..."
    python3 -m venv venv
fi

# Activate virtual environment
echo "🔧 Activating virtual environment..."
source venv/bin/activate

# Install dependencies
echo "📥 Installing dependencies..."
pip install -r requirements.txt

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "⚙️ Creating environment configuration..."
    cp env.example .env
    echo "📝 Please edit .env file if needed"
fi

# Check if IoT system is running
echo "🔍 Checking IoT system status..."

# Check API Gateway
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ API Gateway is running"
else
    echo "❌ API Gateway is not running. Please start the IoT system first."
    echo "   Run: cd .. && ./scripts/start-services.sh"
    exit 1
fi

# Check MQTT broker
if docker ps | grep -q mosquitto; then
    echo "✅ MQTT broker is running"
else
    echo "❌ MQTT broker is not running. Please start the IoT system first."
    exit 1
fi

# Start mock devices
echo "🎯 Starting mock devices..."

# Parse command line arguments
DEVICE_COUNT=10
FACTORY_ID="factory-001"
INTERACTIVE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --count)
            DEVICE_COUNT="$2"
            shift 2
            ;;
        --factory-id)
            FACTORY_ID="$2"
            shift 2
            ;;
        --interactive)
            INTERACTIVE=true
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --count N        Number of devices to create (default: 10)"
            echo "  --factory-id ID  Factory ID for devices (default: factory-001)"
            echo "  --interactive    Run in interactive mode"
            echo "  --help           Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Build command
CMD="python main.py --count $DEVICE_COUNT --factory-id $FACTORY_ID"

if [ "$INTERACTIVE" = true ]; then
    CMD="$CMD --interactive"
fi

echo "🚀 Starting $DEVICE_COUNT mock devices for factory $FACTORY_ID"
echo "📡 Command: $CMD"
echo ""

# Start the simulator
exec $CMD
