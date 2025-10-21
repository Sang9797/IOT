#!/bin/bash

# IoT Microservices Startup Script

echo "🚀 Starting IoT Microservices System..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Build the project
echo "📦 Building the project..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

# Start infrastructure services first
echo "🏗️ Starting infrastructure services..."
docker-compose up -d eureka-server zookeeper kafka mosquitto postgres influxdb redis

# Wait for services to be ready
echo "⏳ Waiting for infrastructure services to be ready..."
sleep 30

# Check if services are running
echo "🔍 Checking service health..."

# Check Eureka
if curl -s http://localhost:8761/actuator/health > /dev/null; then
    echo "✅ Eureka Server is running"
else
    echo "❌ Eureka Server is not responding"
fi

# Check Kafka
if docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
    echo "✅ Kafka is running"
else
    echo "❌ Kafka is not responding"
fi

# Check PostgreSQL
if docker-compose exec postgres pg_isready -U iot_user > /dev/null 2>&1; then
    echo "✅ PostgreSQL is running"
else
    echo "❌ PostgreSQL is not responding"
fi

# Check InfluxDB
if curl -s http://localhost:8086/health > /dev/null; then
    echo "✅ InfluxDB is running"
else
    echo "❌ InfluxDB is not responding"
fi

# Start microservices
echo "🚀 Starting microservices..."
docker-compose up -d api-gateway device-management-service device-processor-service analysis-report-service notification-service

# Wait for services to register
echo "⏳ Waiting for microservices to register with Eureka..."
sleep 60

# Check microservices health
echo "🔍 Checking microservices health..."

services=("api-gateway:8080" "device-management-service:8081" "device-processor-service:8082" "analysis-report-service:8083" "notification-service:8084")

for service in "${services[@]}"; do
    name=$(echo $service | cut -d: -f1)
    port=$(echo $service | cut -d: -f2)
    
    if curl -s http://localhost:$port/actuator/health > /dev/null; then
        echo "✅ $name is running on port $port"
    else
        echo "❌ $name is not responding on port $port"
    fi
done

echo ""
echo "🎉 IoT Microservices System is starting up!"
echo ""
echo "📊 Service URLs:"
echo "  - API Gateway: http://localhost:8080"
echo "  - Eureka Dashboard: http://localhost:8761"
echo "  - InfluxDB UI: http://localhost:8086"
echo "  - Device Management: http://localhost:8081"
echo "  - Device Processor: http://localhost:8082"
echo "  - Analysis Service: http://localhost:8083"
echo "  - Notification Service: http://localhost:8084"
echo ""
echo "📝 To view logs: docker-compose logs -f [service-name]"
echo "🛑 To stop: docker-compose down"
echo ""
echo "🔧 Default credentials:"
echo "  - InfluxDB: admin/admin123"
echo "  - PostgreSQL: iot_user/iot_password"
echo ""
