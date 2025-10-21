package com.iot.deviceprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.common.config.KafkaTopics;
import com.iot.common.dto.DeviceDataDto;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MqttService implements MqttCallback {
    
    @Autowired
    private MqttClient mqttClient;
    
    @Autowired
    private MqttConnectOptions mqttConnectOptions;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String DEVICE_DATA_TOPIC = "devices/+/data";
    private static final String DEVICE_STATUS_TOPIC = "devices/+/status";
    private static final String DEVICE_CONTROL_TOPIC = "devices/+/control";
    
    @PostConstruct
    public void initialize() {
        try {
            mqttClient.setCallback(this);
            mqttClient.connect(mqttConnectOptions);
            
            // Subscribe to device data topics
            mqttClient.subscribe(DEVICE_DATA_TOPIC, 1);
            mqttClient.subscribe(DEVICE_STATUS_TOPIC, 1);
            mqttClient.subscribe(DEVICE_CONTROL_TOPIC, 1);
            
            System.out.println("MQTT Client connected and subscribed to topics");
        } catch (MqttException e) {
            System.err.println("Failed to initialize MQTT client: " + e.getMessage());
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
            System.err.println("Error disconnecting MQTT client: " + e.getMessage());
        }
    }
    
    @Override
    public void connectionLost(Throwable cause) {
        System.err.println("MQTT connection lost: " + cause.getMessage());
        // Auto-reconnect is handled by MqttConnectOptions
    }
    
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            String payload = new String(message.getPayload());
            System.out.println("Received MQTT message from topic: " + topic + ", payload: " + payload);
            
            // Extract device ID from topic (format: devices/{deviceId}/data)
            String[] topicParts = topic.split("/");
            if (topicParts.length >= 2) {
                String deviceId = topicParts[1];
                
                if (topic.endsWith("/data")) {
                    processDeviceData(deviceId, payload);
                } else if (topic.endsWith("/status")) {
                    processDeviceStatus(deviceId, payload);
                } else if (topic.endsWith("/control")) {
                    processControlResponse(deviceId, payload);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing MQTT message: " + e.getMessage());
        }
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("MQTT message delivery complete");
    }
    
    private void processDeviceData(String deviceId, String payload) {
        try {
            // Parse the payload as JSON
            Map<String, Object> dataMap = objectMapper.readValue(payload, Map.class);
            
            // Create DeviceDataDto
            DeviceDataDto deviceData = new DeviceDataDto();
            deviceData.setDeviceId(deviceId);
            deviceData.setTimestamp(LocalDateTime.now());
            deviceData.setData(dataMap);
            
            // Publish to Kafka for further processing
            kafkaTemplate.send(KafkaTopics.DEVICE_DATA_RAW, deviceId, deviceData);
            kafkaTemplate.send(KafkaTopics.MQTT_BRIDGE_DATA, deviceId, deviceData);
            
        } catch (Exception e) {
            System.err.println("Error processing device data: " + e.getMessage());
        }
    }
    
    private void processDeviceStatus(String deviceId, String payload) {
        try {
            Map<String, Object> statusMap = objectMapper.readValue(payload, Map.class);
            
            // Publish device status update to Kafka
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("deviceId", deviceId);
            statusUpdate.put("status", statusMap);
            statusUpdate.put("timestamp", LocalDateTime.now());
            
            kafkaTemplate.send(KafkaTopics.DEVICE_STATUS_CHANGES, deviceId, statusUpdate);
            
        } catch (Exception e) {
            System.err.println("Error processing device status: " + e.getMessage());
        }
    }
    
    private void processControlResponse(String deviceId, String payload) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(payload, Map.class);
            
            // Publish control response to Kafka
            Map<String, Object> controlResponse = new HashMap<>();
            controlResponse.put("deviceId", deviceId);
            controlResponse.put("response", responseMap);
            controlResponse.put("timestamp", LocalDateTime.now());
            
            kafkaTemplate.send(KafkaTopics.DEVICE_COMMAND_RESPONSES, deviceId, controlResponse);
            
        } catch (Exception e) {
            System.err.println("Error processing control response: " + e.getMessage());
        }
    }
    
    public void publishControlCommand(String deviceId, String command) {
        try {
            String topic = "devices/" + deviceId + "/control";
            MqttMessage message = new MqttMessage(command.getBytes());
            message.setQos(1);
            message.setRetained(false);
            
            mqttClient.publish(topic, message);
            System.out.println("Published control command to device " + deviceId + ": " + command);
            
        } catch (MqttException e) {
            System.err.println("Error publishing control command: " + e.getMessage());
        }
    }
    
    public void publishBroadcastCommand(String command) {
        try {
            String topic = "devices/all/control";
            MqttMessage message = new MqttMessage(command.getBytes());
            message.setQos(1);
            message.setRetained(false);
            
            mqttClient.publish(topic, message);
            System.out.println("Published broadcast control command: " + command);
            
        } catch (MqttException e) {
            System.err.println("Error publishing broadcast command: " + e.getMessage());
        }
    }
}
