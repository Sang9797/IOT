package com.iot.deviceprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.common.dto.MqttBrokerDto;
import com.iot.deviceprocessor.config.MqttConfig;
import com.iot.deviceprocessor.mqtt.ManagedMqttClient;
import com.iot.deviceprocessor.mqtt.ManagedMqttClientFactory;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MqttServiceTest {

    @Mock
    private ManagedMqttClientFactory managedMqttClientFactory;

    @Mock
    private ManagedMqttClient broker1Client;

    @Mock
    private ManagedMqttClient broker2Client;

    @Mock
    private MqttConfig mqttConfig;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private MqttService mqttService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        mqttService = new MqttService(managedMqttClientFactory, mqttConfig, objectMapper, kafkaTemplate);

        when(mqttConfig.composeBrokerUrl(any(MqttBrokerDto.class))).thenAnswer(invocation -> {
            MqttBrokerDto broker = invocation.getArgument(0);
            return broker.getProtocol() + "://" + broker.getHost() + ":" + broker.getPort();
        });
        when(mqttConfig.composeClientId(any(String.class))).thenAnswer(invocation -> "device-processor-" + invocation.getArgument(0));
        when(mqttConfig.createConnectOptions(any(MqttBrokerDto.class))).thenReturn(new MqttConnectOptions());
        when(managedMqttClientFactory.create(eq("tcp://host-1:1883"), any(String.class))).thenReturn(broker1Client);
        when(managedMqttClientFactory.create(eq("tcp://host-2:1884"), any(String.class))).thenReturn(broker2Client);
        when(broker1Client.isConnected()).thenReturn(true);
        when(broker2Client.isConnected()).thenReturn(true);
    }

    @Test
    void publishControlCommandUsesAssignedBroker() throws Exception {
        mqttService.handleBrokerUpdate(objectMapper.writeValueAsString(buildBroker("broker-1", "host-1", 1883, true, "line-a")));
        mqttService.handleBrokerUpdate(objectMapper.writeValueAsString(buildBroker("broker-2", "host-2", 1884, true, "line-b")));
        mqttService.handleDeviceMetadataUpdate("{\"id\":\"device-1\",\"mqttBrokerId\":\"broker-2\"}");

        mqttService.publishControlCommand("device-1", "{\"command\":\"restart\"}");

        verify(broker2Client).publish(eq("line-b/devices/device-1/control"), any(MqttMessage.class));
        verify(broker1Client, times(0)).publish(any(String.class), any(MqttMessage.class));
    }

    @Test
    void broadcastPublishesOncePerEnabledBroker() throws Exception {
        mqttService.handleBrokerUpdate(objectMapper.writeValueAsString(buildBroker("broker-1", "host-1", 1883, true, "line-a")));
        mqttService.handleBrokerUpdate(objectMapper.writeValueAsString(buildBroker("broker-2", "host-2", 1884, false, "line-b")));
        mqttService.handleBrokerUpdate(objectMapper.writeValueAsString(buildBroker("broker-3", "host-2", 1884, true, "line-c")));
        when(managedMqttClientFactory.create(eq("tcp://host-2:1884"), any(String.class))).thenReturn(broker2Client);

        mqttService.publishBroadcastCommand("{\"command\":\"sync\"}");

        verify(broker1Client).publish(eq("line-a/devices/all/control"), any(MqttMessage.class));
        verify(broker2Client, times(0)).publish(eq("line-b/devices/all/control"), any(MqttMessage.class));
    }

    private MqttBrokerDto buildBroker(String id, String host, int port, boolean enabled, String topicPrefix) {
        MqttBrokerDto dto = new MqttBrokerDto();
        dto.setId(id);
        dto.setName(id);
        dto.setHost(host);
        dto.setPort(port);
        dto.setProtocol("tcp");
        dto.setEnabled(enabled);
        dto.setTopicPrefix(topicPrefix);
        return dto;
    }
}
