# Standard structure
        Root CA (offline)
              |
        ┌─────┴─────┐
Server CA     Device CA
(sign broker) (sign IoT certs)

# CA STRATEGY
| Component   | Purpose               |
|-------------|-----------------------|
| Root CA     | Sign intermediate     |
| Server CA   | Sign broker cert      |
| Device CA   | Sign IoT cert         |
| Device cert | Auth per each device  |
| CN          | deviceId (map to ACL) |

# Create Root CA
```bash
openssl genrsa -out rootCA.key 4096

openssl req -x509 -new -nodes \
  -key rootCA.key \
  -sha256 -days 3650 \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=IoT Root CA" \
  -out rootCA.crt
```

# Create ServerCA
### Server CA key + CSR
```bash
openssl genrsa -out serverCA.key 4096

openssl req -new \
  -key serverCA.key \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=IoT Server CA" \
  -out serverCA.csr
```

### Extension for ServerCA
```bash
cat > serverCA.ext <<EOF
basicConstraints = CA:TRUE,pathlen:0
keyUsage = critical, keyCertSign, cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
EOF
```

### Create ServerCA.crt
```bash
openssl x509 -req \
  -in serverCA.csr \
  -CA rootCA.crt \
  -CAkey rootCA.key \
  -CAcreateserial \
  -out serverCA.crt \
  -days 3650 \
  -sha256 \
  -extfile serverCA.ext

```

# Create Server Cert (Mosquitto)
### Server key + CSR
```bash
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr \
-subj "/C=US/ST=State/L=City/O=Organization/CN=mosquitto"
```

### Create Server cert extension
```bash
cat > server.ext <<EOF
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = mosquitto
DNS.2 = localhost
IP.1 = 127.0.0.1
EOF
```

### Sign server certificate with serverCA
```bash
openssl x509 -req \
  -in server.csr \
  -CA serverCA.crt \
  -CAkey serverCA.key \
  -CAcreateserial \
  -out server.crt \
  -days 365 \
  -sha256 \
  -extfile server.ext
```
```bash
sudo chmod 640 server.key
sudo chmod 644 server.crt
sudo chown 1883:1883 server.*
```

### Verify cert chain
```bash
openssl verify \
  -CAfile rootCA.crt \
  -untrusted serverCA.crt \
  server.crt
```

# Create Device CA
### deviceCA key + CSR
```bash
openssl genrsa -out deviceCA.key 4096

openssl req -new \
  -key deviceCA.key \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=IoT Device CA" \
  -out deviceCA.csr
```

### deviceCA extension
```bash
cat > deviceCA.ext <<EOF
basicConstraints = CA:TRUE,pathlen:0
keyUsage = critical, keyCertSign, cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
EOF
```

### Root CA sign deviceCA
```bash
openssl x509 -req \
  -in deviceCA.csr \
  -CA rootCA.crt \
  -CAkey rootCA.key \
  -CAcreateserial \
  -out deviceCA.crt \
  -days 3650 \
  -sha256 \
  -extfile deviceCA.ext
```

# Create device extension
```bash
cat > device.ext <<EOF
basicConstraints = CA:FALSE
keyUsage = digitalSignature
extendedKeyUsage = clientAuth
EOF
```

# Generate healthcheck certificate with deviceCA
```bash
openssl genrsa -out healthcheck.key 2048
openssl req -new -key healthcheck.key -out healthcheck.csr \
-subj "/C=US/ST=State/L=City/O=Organization/CN=healthcheck"
```

### deviceCA sign device cert
```bash
openssl x509 -req \
  -in healthcheck.csr \
  -CA deviceCA.crt \
  -CAkey deviceCA.key \
  -CAcreateserial \
  -out healthcheck.crt \
  -days 180 \
  -sha256 \
  -extfile device.ext
```

```bash
sudo chmod 640 healthcheck.key
sudo chmod 644 healthcheck.crt
sudo chown 1883:1883 healthcheck.*
```

### Verify device cert chain
```bash
openssl verify \
  -CAfile rootCA.crt \
  -untrusted deviceCA.crt \
  healthcheck.crt
```

# Generate kafka_bridge certificate with deviceCA
```bash
openssl genrsa -out kafka_bridge.key 2048
openssl req -new -key kafka_bridge.key -out kafka_bridge.csr \
-subj "/C=US/ST=State/L=City/O=Organization/CN=kafka_bridge"
```

### deviceCA sign device cert
```bash
openssl x509 -req \
  -in kafka_bridge.csr \
  -CA deviceCA.crt \
  -CAkey deviceCA.key \
  -CAcreateserial \
  -out kafka_bridge.crt \
  -days 180 \
  -sha256 \
  -extfile device.ext
```

```bash
sudo chmod 640 kafka_bridge.key
sudo chmod 644 kafka_bridge.crt
sudo chown 1883:1883 kafka_bridge.*
```

### Verify device cert chain
```bash
openssl verify \
  -CAfile rootCA.crt \
  -untrusted deviceCA.crt \
  kafka_bridge.crt
```

# Generate iot_device_01 certificate with deviceCA
```bash
openssl genrsa -out iot_device_01.key 2048
openssl req -new -key iot_device_01.key -out iot_device_01.csr \
-subj "/C=US/ST=State/L=City/O=Organization/CN=iot_device_01"
```

### deviceCA sign device cert
```bash
openssl x509 -req \
  -in iot_device_01.csr \
  -CA deviceCA.crt \
  -CAkey deviceCA.key \
  -CAcreateserial \
  -out iot_device_01.crt \
  -days 180 \
  -sha256 \
  -extfile device.ext
```

```bash
sudo chmod 640 kafka_bridge.key
sudo chmod 644 kafka_bridge.crt
sudo chown 1883:1883 kafka_bridge.*
```

### Verify device cert chain
```bash
openssl verify \
  -CAfile rootCA.crt \
  -untrusted deviceCA.crt \
  kafka_bridge.crt
```

# Generate 10,000 devices
```bash
for i in $(seq -w 1 10000); do
  DEVICE_ID=device-$i

  openssl genrsa -out $DEVICE_ID.key 2048

  openssl req -new \
    -key $DEVICE_ID.key \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=$DEVICE_ID" \
    -out $DEVICE_ID.csr

  openssl x509 -req \
    -in $DEVICE_ID.csr \
    -CA deviceCA.crt -CAkey deviceCA.key \
    -CAcreateserial \
    -out $DEVICE_ID.crt \
    -days 365 \
    -sha256 \
    -extfile device.ext
done
```

# MOSQUITTO CONFIG
### Create file cert full chain
```bash
cat rootCA.crt deviceCA.crt > device-ca-bundle.crt
cat rootCA.crt serverCa.crt > ca-bundle.crt
```
```text
mosquitto-tls.conf
```

# Test flow with certificate
### Subscribe topic
```bash
mosquitto_sub -h localhost -p 8883 \
           --cafile /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/ca-bundle.crt \
           --cert /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/kafka_bridge.crt \
           --key /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/kafka_bridge.key \
           -t "iot/sensors/#"
```

### Publish message to topic
```bash
mosquitto_pub -h localhost -p 8883 -t "iot/sensors/iot_device_01/temp" \
          --cafile /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/ca-bundle.crt \
          --cert /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/iot_device_01.crt \
          --key /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/iot_device_01.key \
          -m '{"temp":25.5}'  
```

# Update MQTT Source Connector with authentication for TLS connection
### Convert certificates to PKCS12 first
```bash
openssl pkcs12 -export \
-in mosquitto/certs/kafka/kafka_bridge.crt \
-inkey mosquitto/certs/kafka/kafka_bridge.key \
-out kafka-connect/kafka_bridge.p12 \
-name kafka_bridge \
-passout pass:kafka1234
```

### Convert CA bundle to truststore
```bash
keytool -importcert \
  -file mosquitto/config-extend/cert/rootCA.crt \
  -alias root-ca \
  -keystore kafka-connect/truststore.jks \
  -storepass kafka1234 \
  -noprompt

keytool -importcert \
  -file mosquitto/config-extend/cert/serverCA.crt \
  -alias server-ca \
  -keystore kafka-connect/truststore.jks \
  -storepass kafka1234 \
  -noprompt
```

```bash
keytool -list -keystore kafka-connect/truststore.jks
```

### Convert PKCS12 to JKS keystore
```bash
openssl pkcs12 -export \
  -in mosquitto/config-extend/cert/kafka_bridge.crt \
  -inkey mosquitto/config-extend/cert/kafka_bridge.key \
  -out kafka-connect/kafka_bridge.p12 \
  -name kafka_bridge \
  -passout pass:kafka1234  
```
```bash
keytool -importkeystore \
  -srckeystore kafka-connect/kafka_bridge.p12 \
  -srcstoretype PKCS12 \
  -srcstorepass kafka1234 \
  -destkeystore kafka-connect/keystore.jks \
  -deststorepass kafka1234 \
  -noprompt  
```

### Check what SSL properties the connector actually supports
```bash
curl http://localhost:8083/connector-plugins/io.confluent.connect.mqtt.MqttSourceConnector/config/validate \
  -X PUT \
  -H "Content-Type: application/json" \
  -d '{
    "connector.class": "io.confluent.connect.mqtt.MqttSourceConnector",
    "mqtt.server.uri": "ssl://mosquitto:8883"
  }' | jq '.configs[] | select(.definition.name | contains("ssl")) | .definition.name'
```

### Create connector
```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
  "name": "mqtt-source-tls",
  "config": {
    "connector.class": "io.confluent.connect.mqtt.MqttSourceConnector",
    "tasks.max": "1",
    "mqtt.server.uri": "ssl://mosquitto:8883",
    "mqtt.topics": "iot/sensors/#",
    "kafka.topic": "mqtt-messages",
    "mqtt.qos": "1",
    "mqtt.clean.session.enabled": "true",
    "mqtt.ssl.trust.store.path": "/certs/truststore.jks",
    "mqtt.ssl.trust.store.password": "kafka1234",
    "mqtt.ssl.key.store.path": "/certs/keystore.jks",
    "mqtt.ssl.key.store.password": "kafka1234",
    "mqtt.ssl.key.password": "kafka1234",
    "confluent.topic.bootstrap.servers": "kafka:29092",
    "confluent.topic.replication.factor": "1"
  }
}'

```

### Verify connector
```bash
curl -X GET http://localhost:8083/connectors | jq
```

# Verify flow MQTT -> MQTT_BRIDGE -> KAFKA
### Verify Messages in Kafka
```bash
docker exec -it kafka bash
```
### Create and consume from the Kafka topic
```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
--topic mqtt-messages \
--from-beginning \
--property print.key=true \
--property print.timestamp=true
```

### Subscribe topic
```bash
mosquitto_sub -h localhost -p 8883 \
           --cafile /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/ca-bundle.crt \
           --cert /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/kafka_bridge.crt \
           --key /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/kafka_bridge.key \
           -t "iot/sensors/#"
```

### Publish message to topic
```bash
mosquitto_pub -h localhost -p 8883 -t "iot/sensors/iot_device_01/temp" \
          --cafile /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/ca-bundle.crt \
          --cert /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/iot_device_01.crt \
          --key /home/sanglee/cursor/IOT/mosquitto/config-extend/cert/iot_device_01.key \
          -m '{"temp":25.5}'  
```



