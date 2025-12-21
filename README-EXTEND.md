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

# Test with Mock Device
### Subscribe to MQTT for Debugging
```bash
mosquitto_sub -h localhost -p 1883 -t "iot/sensors/#" -v
```
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

# Using mqtt with authentication and authorization
### Install mosquitto
```bash
sudo apt update
sudo apt install mosquitto
```

### Create password file on host
```bash
mosquitto_passwd mosquitto/config/passwd healthcheck
mosquitto_passwd -c mosquitto/config/passwd iot_device_01
mosquitto_passwd mosquitto/config/passwd admin_user
mosquitto_passwd mosquitto/config/passwd kafka_bridge
```
### Update mosquitto.conf
```
# Add this line to your config
password_file /mosquitto/config/passwd
```

### Restart and Test
```bash
docker compose -f docker-compose-system.yml restart mosquitto
```

# Test Authentication
### subscribe
```bash
mosquitto_sub -h localhost -p 1883 -t "iot/sensors/#" -u iot_device_01 -P SecurePass123!
```
### publish
```bash
mosquitto_pub -h localhost -p 1883 -t "iot/sensors/temperature" -m '{"device":"sensor-01","temperature":24.1,"humidity":63,"timestamp":"2024-12-20T10:01:00Z"}' -u iot_device_01 -P SecurePass123!
```

# Access Control Lists (ACL) for Fine-Grained Permissions
### Create ACL File
**Create mosquitto/config/acl**
**CL Syntax:**
1. # = multi-level wildcard (matches any number of levels)
2. + = single-level wildcard (matches one level)
3. %u = username placeholder
4. read = subscribe permission
5. write = publish permission
6. readwrite = both permissions
### Update mosquitto.conf
```
# Add this line to your config
acl_file /mosquitto/config/acl
```
### Restart and Test
```bash
docker compose -f docker-compose-system.yml restart mosquitto
```
### Test: iot_device_01 can publish to its topic
```bash
mosquitto_pub -h localhost -p 1883 -t "iot/sensors/device-01/temp" \
  -m '{"temp":25.5}' -u iot_device_01 -P SecurePass123!
```
### Test: iot_device_01 CANNOT publish to device-02's topic
```bash
mosquitto_pub -h localhost -p 1883 -t "iot/sensors/device-02/temp" \
  -m '{"temp":25.5}' -u iot_device_01 -P SecurePass123!
```
### Test: kafka_bridge can subscribe to all sensors
```bash 
mosquitto_sub -h localhost -p 1883 -t "iot/sensors/#" \
-u kafka_bridge -P BridgePass123!
```
### Create topic for healthcheck
```bash
docker exec mosquitto mosquitto_pub \
  -h localhost -p 1883 \
  -u admin_user -P BridgePass123! \
  -t health/ping -m ok -r
```

### Update MQTT Source Connector with Authentication
```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "mqtt-source",
    "config": {
      "connector.class": "io.confluent.connect.mqtt.MqttSourceConnector",
      "tasks.max": "1",
      "mqtt.server.uri": "tcp://mosquitto:1883",
      "mqtt.username": "kafka_bridge",
      "mqtt.password": "BridgePass123!",
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
curl -X GET http://localhost:8083/connectors | jq
```
