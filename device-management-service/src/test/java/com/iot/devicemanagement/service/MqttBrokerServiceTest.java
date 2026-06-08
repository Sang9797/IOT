package com.iot.devicemanagement.service;

import com.iot.common.dto.MqttBrokerDto;
import com.iot.devicemanagement.entity.MqttBroker;
import com.iot.devicemanagement.exception.ConflictException;
import com.iot.devicemanagement.repository.DeviceRepository;
import com.iot.devicemanagement.repository.MqttBrokerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MqttBrokerServiceTest {

    @Mock
    private MqttBrokerRepository mqttBrokerRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private MqttBrokerService mqttBrokerService;

    @BeforeEach
    void setUp() {
        mqttBrokerService = new MqttBrokerService(mqttBrokerRepository, deviceRepository, kafkaTemplate);
    }

    @Test
    void deleteBrokerIsBlockedWhenDevicesExist() {
        MqttBroker broker = new MqttBroker();
        broker.setId("broker-1");

        when(mqttBrokerRepository.findById("broker-1")).thenReturn(Optional.of(broker));
        when(deviceRepository.countByMqttBroker_Id("broker-1")).thenReturn(2L);

        assertThrows(ConflictException.class, () -> mqttBrokerService.deleteBroker("broker-1"));
        verify(mqttBrokerRepository, never()).delete(broker);
    }

    @Test
    void updateBrokerPublishesUpdate() {
        MqttBroker broker = new MqttBroker();
        broker.setId("broker-1");
        broker.setName("Old");
        broker.setHost("old-host");
        broker.setPort(1883);
        broker.setProtocol("tcp");
        broker.setEnabled(true);

        MqttBrokerDto request = new MqttBrokerDto();
        request.setName("New");
        request.setHost("new-host");
        request.setPort(8883);
        request.setProtocol("ssl");
        request.setEnabled(true);

        when(mqttBrokerRepository.findById("broker-1")).thenReturn(Optional.of(broker));
        when(mqttBrokerRepository.findByName("New")).thenReturn(Optional.empty());
        when(mqttBrokerRepository.save(broker)).thenReturn(broker);

        mqttBrokerService.updateBroker("broker-1", request);

        verify(kafkaTemplate).send(any(String.class), any(String.class), any(MqttBrokerDto.class));
    }
}
