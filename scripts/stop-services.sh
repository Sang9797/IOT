#!/bin/bash

# IoT Microservices Shutdown Script

echo "🛑 Stopping IoT Microservices System..."

# Stop all services
docker-compose down

# Remove volumes if requested
if [ "$1" = "--clean" ]; then
    echo "🧹 Cleaning up volumes..."
    docker-compose down -v
    docker volume prune -f
fi

echo "✅ All services stopped successfully!"
echo ""
echo "💡 To start services again: ./scripts/start-services.sh"
echo "🧹 To clean up volumes: ./scripts/stop-services.sh --clean"
