# Create the connector instance
```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
  "name": "mqtt-source",
  "config": {
  "connector.class": "io.confluent.connect.mqtt.MqttSourceConnector",
  "tasks.max": "1",
  "mqtt.server.uri": "tcp://mosquitto:1883",
  "mqtt.topics": "iot/sensors/#",
  "kafka.topic": "mqtt-messages",
  "mqtt.qos": "1",
  "mqtt.clean.session.enabled": "true",
  "confluent.topic.bootstrap.servers": "kafka:29092",
  "confluent.topic.replication.factor": "1"
  }
}'
```
```bash
curl -X GET http://localhost:8083/connectors
```

# Check connector status
```bash
curl http://localhost:8083/connectors/mqtt-source/status | jq
```

# Subscribe to MQTT for Debugging
```bash
mosquitto_sub -h localhost -p 1883 -t "iot/sensors/#" -v
```

# Verify Messages in Kafka
```bash
docker exec -it kafka bash
```
### Create and consume from the Kafka topic
kafka-console-consumer --bootstrap-server localhost:9092 \
--topic mqtt-messages \
--from-beginning \
--property print.key=true \
--property print.timestamp=true

# Test with Mock Device
### Send a single message
```bash
mosquitto_pub -h localhost -p 1883 -t "iot/sensors/temperature" -m '{"device":"sensor-01","temperature":23.5,"humidity":65,"timestamp":"2024-12-20T10:00:00Z"}'
```
### Send multiple messages
```bash
mosquitto_pub -h localhost -p 1883 -t "iot/sensors/temperature" -m '{"device":"sensor-01","temperature":24.1,"humidity":63,"timestamp":"2024-12-20T10:01:00Z"}'
mosquitto_pub -h localhost -p 1883 -t "iot/sensors/pressure" -m '{"device":"sensor-02","pressure":1013.25,"location":"room-a","timestamp":"2024-12-20T10:01:00Z"}'
mosquitto_pub -h localhost -p 1883 -t "iot/sensors/motion" -m '{"device":"sensor-03","motion":true,"location":"entrance","timestamp":"2024-12-20T10:02:00Z"}'
```
