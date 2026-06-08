package com.iot.devicemanagement.service;

import com.iot.common.dto.DeviceDto;
import com.iot.devicemanagement.entity.Device;
import com.iot.devicemanagement.entity.MqttBroker;
import com.iot.devicemanagement.exception.ValidationException;
import com.iot.devicemanagement.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private MqttBrokerService mqttBrokerService;

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService(deviceRepository, kafkaTemplate, mqttBrokerService);
    }

    @Test
    void createDeviceStoresBrokerAssignment() {
        DeviceDto request = new DeviceDto();
        request.setName("Device 1");
        request.setAddress("192.168.1.10");
        request.setType(DeviceDto.DeviceType.SENSOR);
        request.setStatus(DeviceDto.DeviceStatus.ONLINE);
        request.setMqttBrokerId("broker-1");

        MqttBroker broker = new MqttBroker();
        broker.setId("broker-1");
        broker.setName("Primary Broker");
        broker.setHost("mqtt.local");
        broker.setPort(1883);
        broker.setProtocol("tcp");

        when(mqttBrokerService.getRequiredEnabledBroker("broker-1")).thenReturn(broker);
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device device = invocation.getArgument(0);
            device.setId("device-1");
            return device;
        });

        DeviceDto created = deviceService.createDevice(request);

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(deviceRepository).save(captor.capture());
        assertEquals("broker-1", captor.getValue().getMqttBroker().getId());
        assertEquals("broker-1", created.getMqttBrokerId());
        verify(kafkaTemplate).send(any(String.class), any(String.class), any(DeviceDto.class));
    }

    @Test
    void createDeviceRejectsUnknownBroker() {
        DeviceDto request = new DeviceDto();
        request.setName("Device 1");
        request.setAddress("192.168.1.10");
        request.setType(DeviceDto.DeviceType.SENSOR);
        request.setStatus(DeviceDto.DeviceStatus.ONLINE);
        request.setMqttBrokerId("missing-broker");

        when(mqttBrokerService.getRequiredEnabledBroker("missing-broker"))
                .thenThrow(new ValidationException("mqttBrokerId is required"));

        assertThrows(ValidationException.class, () -> deviceService.createDevice(request));
    }
}
