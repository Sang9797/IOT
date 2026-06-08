package com.iot.deviceprocessor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.common.config.KafkaHeaderNames;
import com.iot.common.config.KafkaTopics;
import com.iot.common.dto.DeviceDto;
import com.iot.common.dto.MqttBrokerDto;
import com.iot.deviceprocessor.config.MqttConfig;
import com.iot.deviceprocessor.mqtt.ManagedMqttClient;
import com.iot.deviceprocessor.mqtt.ManagedMqttClientFactory;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MqttService {

    private static final Logger log = LoggerFactory.getLogger(MqttService.class);
    private static final int DEFAULT_QOS = 1;
    private static final String DEFAULT_SENSOR_TOPIC = "iot/sensors/#";

    private final ManagedMqttClientFactory managedMqttClientFactory;
    private final MqttConfig mqttConfig;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Map<String, MqttBrokerDto> brokersById = new ConcurrentHashMap<>();
    private final Map<String, String> deviceBrokerAssignments = new ConcurrentHashMap<>();
    private final Map<String, DeviceDto> devicesById = new ConcurrentHashMap<>();
    private final Map<String, ManagedBrokerClient> brokerClients = new ConcurrentHashMap<>();

    public MqttService(
            ManagedMqttClientFactory managedMqttClientFactory,
            MqttConfig mqttConfig,
            ObjectMapper objectMapper,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.managedMqttClientFactory = managedMqttClientFactory;
        this.mqttConfig = mqttConfig;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.DEVICE_METADATA_UPDATES, groupId = "device-processor-device-metadata")
    public void handleDeviceMetadataUpdate(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root.hasNonNull("mqttBrokerId") && root.hasNonNull("id")) {
                DeviceDto device = objectMapper.treeToValue(root, DeviceDto.class);
                devicesById.put(device.getId(), device);
                deviceBrokerAssignments.put(device.getId(), device.getMqttBrokerId());
                ensureBrokerConnected(device.getMqttBrokerId());
                return;
            }
            if (root.hasNonNull("deviceId") && !root.hasNonNull("mqttBrokerId")) {
                String deviceId = root.get("deviceId").asText();
                deviceBrokerAssignments.remove(deviceId);
                devicesById.remove(deviceId);
            }
        } catch (Exception exception) {
            log.warn("Failed to process device metadata update payload: {}", payload, exception);
        }
    }

    @KafkaListener(topics = KafkaTopics.MQTT_BROKER_UPDATES, groupId = "device-processor-broker-metadata")
    public void handleBrokerUpdate(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root.hasNonNull("brokerId") && root.size() == 1) {
                removeBroker(root.get("brokerId").asText());
                return;
            }
            MqttBrokerDto broker = objectMapper.treeToValue(root, MqttBrokerDto.class);
            brokersById.put(broker.getId(), broker);
            if (Boolean.TRUE.equals(broker.getEnabled())) {
                refreshBrokerClient(broker);
            } else {
                disconnectBroker(broker.getId());
            }
        } catch (Exception exception) {
            log.warn("Failed to process broker metadata update payload: {}", payload, exception);
        }
    }

    public void publishControlCommand(String deviceId, String command) {
        String brokerId = deviceBrokerAssignments.get(deviceId);
        if (brokerId == null || brokerId.isBlank()) {
            throw new IllegalStateException("No MQTT broker assignment exists for device " + deviceId);
        }

        MqttBrokerDto broker = brokersById.get(brokerId);
        if (broker == null) {
            throw new IllegalStateException("Broker " + brokerId + " is not loaded in the processor");
        }
        if (!Boolean.TRUE.equals(broker.getEnabled())) {
            throw new IllegalStateException("Broker " + brokerId + " is disabled");
        }

        publishToBroker(broker, buildTopic(broker.getTopicPrefix(), "devices/" + deviceId + "/control"), command);
    }

    public void publishBroadcastCommand(String command) {
        boolean published = false;
        for (MqttBrokerDto broker : brokersById.values()) {
            if (!Boolean.TRUE.equals(broker.getEnabled())) {
                continue;
            }
            publishToBroker(broker, buildTopic(broker.getTopicPrefix(), "devices/all/control"), command);
            published = true;
        }
        if (!published) {
            throw new IllegalStateException("No enabled MQTT brokers are available");
        }
    }

    public int getConnectedBrokerCount() {
        return (int) brokerClients.values().stream().filter(ManagedBrokerClient::isConnected).count();
    }

    @PreDestroy
    public void cleanup() {
        brokerClients.keySet().forEach(this::disconnectBroker);
    }

    private synchronized void refreshBrokerClient(MqttBrokerDto broker) {
        disconnectBroker(broker.getId());
        ensureBrokerConnected(broker.getId());
    }

    private synchronized void ensureBrokerConnected(String brokerId) {
        if (brokerId == null || brokerId.isBlank()) {
            return;
        }
        MqttBrokerDto broker = brokersById.get(brokerId);
        if (broker == null || !Boolean.TRUE.equals(broker.getEnabled())) {
            return;
        }

        ManagedBrokerClient existing = brokerClients.get(brokerId);
        if (existing != null && existing.isConnected()) {
            return;
        }

        try {
            ManagedMqttClient client = managedMqttClientFactory.create(
                    mqttConfig.composeBrokerUrl(broker),
                    mqttConfig.composeClientId(broker.getId())
            );
            client.setCallback(new BrokerCallback(broker));
            client.connect(mqttConfig.createConnectOptions(broker));
            client.subscribe(buildTopic(broker.getTopicPrefix(), DEFAULT_SENSOR_TOPIC), DEFAULT_QOS);
            brokerClients.put(brokerId, new ManagedBrokerClient(broker, client));
            log.info("Connected MQTT client for broker {}", brokerId);
        } catch (MqttException exception) {
            throw new IllegalStateException("Failed to connect broker " + brokerId, exception);
        }
    }

    private void publishToBroker(MqttBrokerDto broker, String topic, String command) {
        ensureBrokerConnected(broker.getId());
        ManagedBrokerClient client = brokerClients.get(broker.getId());
        if (client == null || !client.isConnected()) {
            throw new IllegalStateException("Broker " + broker.getId() + " is not connected");
        }
        try {
            MqttMessage message = new MqttMessage(command.getBytes(StandardCharsets.UTF_8));
            message.setQos(DEFAULT_QOS);
            message.setRetained(false);
            client.client().publish(topic, message);
            log.info("Published MQTT command via broker {} to topic {}", broker.getId(), topic);
        } catch (MqttException exception) {
            throw new IllegalStateException("Failed to publish via broker " + broker.getId(), exception);
        }
    }

    private void removeBroker(String brokerId) {
        brokersById.remove(brokerId);
        disconnectBroker(brokerId);
        deviceBrokerAssignments.entrySet().removeIf(entry -> brokerId.equals(entry.getValue()));
        devicesById.entrySet().removeIf(entry -> brokerId.equals(entry.getValue().getMqttBrokerId()));
    }

    private synchronized void disconnectBroker(String brokerId) {
        ManagedBrokerClient client = brokerClients.remove(brokerId);
        if (client == null) {
            return;
        }
        try {
            if (client.client().isConnected()) {
                client.client().disconnect();
            }
            client.client().close();
            log.info("Disconnected MQTT client for broker {}", brokerId);
        } catch (MqttException exception) {
            log.warn("Failed to disconnect broker {}", brokerId, exception);
        }
    }

    private String buildTopic(String topicPrefix, String topic) {
        if (topicPrefix == null || topicPrefix.isBlank()) {
            return topic;
        }
        return topicPrefix.replaceAll("/+$", "") + "/" + topic.replaceAll("^/+", "");
    }

    private record ManagedBrokerClient(MqttBrokerDto broker, ManagedMqttClient client) {
        boolean isConnected() {
            return client.isConnected();
        }
    }

    private class BrokerCallback implements MqttCallback {

        private final MqttBrokerDto broker;

        private BrokerCallback(MqttBrokerDto broker) {
            this.broker = broker;
        }

        @Override
        public void connectionLost(Throwable cause) {
            log.warn("MQTT connection lost for broker {}: {}", broker.getId(), cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            log.info("Received MQTT message from broker {} topic {} payload {}", broker.getId(), topic, payload);
            publishIngressRecord(broker, topic, payload);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            log.debug("MQTT delivery complete for broker {}", broker.getId());
        }
    }

    private void publishIngressRecord(MqttBrokerDto broker, String topic, String payload) {
        String traceId = UUID.randomUUID().toString();
        String deviceId = extractDeviceId(payload);
        DeviceDto device = deviceId == null ? null : devicesById.get(deviceId);
        String factoryId = device != null ? device.getFactoryId() : null;
        String sourceBridgeId = "mqtt-bridge-" + broker.getId();

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.MQTT_INGRESS,
                deviceId != null ? deviceId : broker.getId(),
                payload
        );
        RecordHeaders headers = (RecordHeaders) record.headers();
        headers.add(KafkaHeaderNames.TRACE_ID, traceId.getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.SOURCE_TOPIC, topic.getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.MQTT_BROKER_ID, broker.getId().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.SOURCE_BRIDGE_ID, sourceBridgeId.getBytes(StandardCharsets.UTF_8));
        if (deviceId != null && !deviceId.isBlank()) {
            headers.add(KafkaHeaderNames.DEVICE_ID, deviceId.getBytes(StandardCharsets.UTF_8));
        }
        if (factoryId != null && !factoryId.isBlank()) {
            headers.add(KafkaHeaderNames.FACTORY_ID, factoryId.getBytes(StandardCharsets.UTF_8));
        }
        kafkaTemplate.send(record);
    }

    private String extractDeviceId(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root.hasNonNull("deviceId")) {
                return root.get("deviceId").asText();
            }
        } catch (Exception exception) {
            log.debug("Unable to extract deviceId from MQTT payload", exception);
        }
        return null;
    }
}
