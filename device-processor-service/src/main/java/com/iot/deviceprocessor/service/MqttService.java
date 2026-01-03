package com.iot.deviceprocessor.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MqttService implements MqttCallback {

    private final MqttClient mqttClient;

    private final MqttConnectOptions mqttConnectOptions;

    private static final String DEVICE_DATA_TOPIC = "iot/sensors/#";

    @PostConstruct
    public void initialize() {
        try {
            mqttClient.setCallback(this);
            mqttClient.connect(mqttConnectOptions);

            // Subscribe to device data topics
            mqttClient.subscribe(DEVICE_DATA_TOPIC, 1);

            log.info("MQTT Client connected and subscribed to topics");
        } catch (MqttException e) {
            log.error("Failed to initialize MQTT client: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (MqttException e) {
            log.error("Error disconnecting MQTT client: {}", e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("MQTT connection lost: {}", cause.getMessage());
        // Auto-reconnect is handled by MqttConnectOptions
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            String payload = new String(message.getPayload());
            log.info("Received MQTT message from topic: {}, payload: {}", topic, payload);
        } catch (Exception e) {
            log.error("Error processing MQTT message: {}", e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("MQTT message delivery complete");
    }

    public void publishControlCommand(String deviceId, String command) {
        try {
            String topic = "devices/" + deviceId + "/control";
            MqttMessage message = new MqttMessage(command.getBytes());
            message.setQos(1);
            message.setRetained(false);

            mqttClient.publish(topic, message);
            log.info("Published control command to device {}: {}", deviceId, command);

        } catch (MqttException e) {
            log.error("Error publishing control command: {}", e.getMessage());
        }
    }

    public void publishBroadcastCommand(String command) {
        try {
            String topic = "devices/all/control";
            MqttMessage message = new MqttMessage(command.getBytes());
            message.setQos(1);
            message.setRetained(false);

            mqttClient.publish(topic, message);
            log.info("Published broadcast control command: {}", command);

        } catch (MqttException e) {
            log.error("Error publishing broadcast command: {}", e.getMessage());
        }
    }
}
