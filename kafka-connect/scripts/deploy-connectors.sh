#!/bin/sh

echo "Waiting for Kafka Connect to be ready..."
sleep 10

CONNECT_URL="http://mqtt-kafka-bridge:8083"

# Function to deploy a connector
deploy_connector() {
    CONNECTOR_FILE=$1
    # Get connector name from JSON file
    CONNECTOR_NAME=$(jq -r '.name' "/connectors/$CONNECTOR_FILE")

    echo "========================================="
    echo "Deploying connector: $CONNECTOR_NAME"
    echo "========================================="

    # Check if connector already exists
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$CONNECT_URL/connectors/$CONNECTOR_NAME")

    if [ "$STATUS" = "200" ]; then
        echo "Connector $CONNECTOR_NAME already exists, updating config..."

        # Extract only the config object and make it compact (single line)
        CONFIG=$(jq -c '.config' "/connectors/$CONNECTOR_FILE")

        echo "Sending update request..."
        RESPONSE=$(curl -s -X PUT \
             -H "Content-Type: application/json" \
             -d "$CONFIG" \
             "$CONNECT_URL/connectors/$CONNECTOR_NAME/config")

        echo "Response:"
        echo "$RESPONSE" | jq '.'
    else
        echo "Creating new connector $CONNECTOR_NAME..."
        RESPONSE=$(curl -s -X POST \
             -H "Content-Type: application/json" \
             -d @"/connectors/$CONNECTOR_FILE" \
             "$CONNECT_URL/connectors")
        echo "Response:"
        echo "$RESPONSE" | jq '.'
    fi

    echo ""
    echo "Checking connector status..."
    curl -s "$CONNECT_URL/connectors/$CONNECTOR_NAME/status" | jq '.'
    echo "========================================="
    echo ""
}

# Wait for Kafka Connect to be fully ready
echo "Ensuring Kafka Connect is fully initialized..."
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$CONNECT_URL/connectors")
    if [ "$RESPONSE" = "200" ]; then
        echo "Kafka Connect is ready!"
        echo ""
        break
    fi
    echo "Waiting for Kafka Connect... (attempt $((ATTEMPT+1))/$MAX_ATTEMPTS)"
    sleep 2
    ATTEMPT=$((ATTEMPT+1))
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    echo "ERROR: Kafka Connect did not become ready in time"
    exit 1
fi

# Deploy all connector configurations
DEPLOYED=0
for connector_file in /connectors/*.json; do
    if [ -f "$connector_file" ]; then
        deploy_connector "$(basename "$connector_file")"
        DEPLOYED=$((DEPLOYED+1))
    fi
done

if [ $DEPLOYED -eq 0 ]; then
    echo "WARNING: No connector files found in /connectors"
else
    echo "Deployment completed! Processed $DEPLOYED connector(s)."
fi

# List all connectors
echo ""
echo "Current connectors:"
curl -s "$CONNECT_URL/connectors" | jq '.'
echo ""