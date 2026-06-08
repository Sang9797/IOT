package com.iot.devicemanagement.service;

import com.iot.common.config.KafkaTopics;
import com.iot.common.config.KafkaHeaderNames;
import com.iot.common.dto.DeviceDto;
import com.iot.common.dto.DeviceStatusEventDto;
import com.iot.devicemanagement.entity.Device;
import com.iot.devicemanagement.entity.MqttBroker;
import com.iot.devicemanagement.exception.NotFoundException;
import com.iot.devicemanagement.repository.DeviceRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MqttBrokerService mqttBrokerService;

    public DeviceService(
            DeviceRepository deviceRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            MqttBrokerService mqttBrokerService
    ) {
        this.deviceRepository = deviceRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.mqttBrokerService = mqttBrokerService;
    }

    public DeviceDto createDevice(DeviceDto deviceDto) {
        Device device = new Device();
        device.setName(deviceDto.getName());
        device.setAddress(deviceDto.getAddress());
        device.setType(deviceDto.getType());
        device.setStatus(deviceDto.getStatus());
        device.setFactoryId(deviceDto.getFactoryId());
        device.setLocation(deviceDto.getLocation());
        device.updateFromDto(deviceDto);
        device.setLastSeen(LocalDateTime.now());
        device.setMqttBroker(resolveBroker(deviceDto.getMqttBrokerId()));

        Device savedDevice = deviceRepository.save(device);
        publishDeviceMetadataUpdate(savedDevice);
        return savedDevice.toDto();
    }
    
    public Optional<DeviceDto> getDeviceById(String id) {
        return deviceRepository.findById(id)
                .map(Device::toDto);
    }
    
    public Optional<DeviceDto> getDeviceByAddress(String address) {
        return deviceRepository.findByAddress(address)
                .map(Device::toDto);
    }
    
    public List<DeviceDto> getAllDevices(String mqttBrokerId) {
        List<Device> devices = mqttBrokerId == null || mqttBrokerId.isBlank()
                ? deviceRepository.findAll()
                : deviceRepository.findByMqttBroker_Id(mqttBrokerId);
        return devices.stream()
                .map(Device::toDto)
                .collect(Collectors.toList());
    }
    
    public List<DeviceDto> getDevicesByFactory(String factoryId) {
        return deviceRepository.findByFactoryId(factoryId).stream()
                .map(Device::toDto)
                .collect(Collectors.toList());
    }
    
    public List<DeviceDto> getDevicesByStatus(DeviceDto.DeviceStatus status) {
        return deviceRepository.findByStatus(status).stream()
                .map(Device::toDto)
                .collect(Collectors.toList());
    }
    
    public DeviceDto updateDevice(String id, DeviceDto deviceDto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Device not found with id: " + id));

        device.updateFromDto(deviceDto);
        device.setMqttBroker(resolveBroker(deviceDto.getMqttBrokerId()));
        Device updatedDevice = deviceRepository.save(device);
        publishDeviceMetadataUpdate(updatedDevice);
        return updatedDevice.toDto();
    }
    
    public void updateDeviceStatus(String id, DeviceDto.DeviceStatus status) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Device not found with id: " + id));

        DeviceDto.DeviceStatus oldStatus = device.getStatus();
        device.setStatus(status);
        device.setLastSeen(LocalDateTime.now());
        deviceRepository.save(device);
        if (!oldStatus.equals(status)) {
            publishDeviceStatusChange(device, oldStatus, status);
        }
    }
    
    public void updateLastSeen(String id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Device not found with id: " + id));

        device.setLastSeen(LocalDateTime.now());
        deviceRepository.save(device);
    }
    
    public void deleteDevice(String id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Device not found with id: " + id));

        deviceRepository.delete(device);
        publishDeviceDeletion(device);
    }
    
    public List<DeviceDto> getOfflineDevices(int minutesThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesThreshold);
        return deviceRepository.findDevicesNotSeenSince(threshold).stream()
                .map(Device::toDto)
                .collect(Collectors.toList());
    }
    
    public long getDeviceCountByFactory(String factoryId) {
        return deviceRepository.countByFactoryId(factoryId);
    }
    
    public long getDeviceCountByStatus(DeviceDto.DeviceStatus status) {
        return deviceRepository.countByStatus(status);
    }

    public List<DeviceDto> getDevicesByBroker(String brokerId) {
        return deviceRepository.findByMqttBroker_Id(brokerId).stream().map(Device::toDto).toList();
    }
    
    private void publishDeviceMetadataUpdate(Device device) {
        DeviceDto deviceDto = device.toDto();
        kafkaTemplate.send(KafkaTopics.DEVICE_METADATA_UPDATES, device.getId(), deviceDto);
    }
    
    private void publishDeviceStatusChange(Device device, DeviceDto.DeviceStatus oldStatus, DeviceDto.DeviceStatus newStatus) {
        String traceId = UUID.randomUUID().toString();
        DeviceStatusEventDto event = new DeviceStatusEventDto();
        event.setEventId(UUID.randomUUID().toString());
        event.setTraceId(traceId);
        event.setSchemaVersion("1.0");
        event.setProducerService("device-management-service");
        event.setPipeline("high-integrity");
        event.setDeviceId(device.getId());
        event.setDeviceName(device.getName());
        event.setFactoryId(device.getFactoryId());
        event.setPreviousStatus(oldStatus);
        event.setCurrentStatus(newStatus);
        event.setChangedAt(LocalDateTime.now());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.DEVICE_STATUS_CHANGES,
                device.getId(),
                event
        );
        RecordHeaders headers = (RecordHeaders) record.headers();
        headers.add(KafkaHeaderNames.EVENT_ID, event.getEventId().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.TRACE_ID, traceId.getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.EVENT_TYPE, "device-status-event".getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.PIPELINE, "high-integrity".getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.SCHEMA_VERSION, event.getSchemaVersion().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.PRODUCER_SERVICE, event.getProducerService().getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record);
    }
    
    private void publishDeviceDeletion(Device device) {
        DeviceDeletionEvent event = new DeviceDeletionEvent(
                device.getId(),
                device.getName(),
                device.getFactoryId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(KafkaTopics.DEVICE_METADATA_UPDATES, device.getId(), event);
    }

    private MqttBroker resolveBroker(String brokerId) {
        return mqttBrokerService.getRequiredEnabledBroker(brokerId);
    }
    
    public static class DeviceDeletionEvent {
        private String deviceId;
        private String deviceName;
        private String factoryId;
        private LocalDateTime timestamp;
        
        public DeviceDeletionEvent() {}
        
        public DeviceDeletionEvent(String deviceId, String deviceName, String factoryId, LocalDateTime timestamp) {
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.factoryId = factoryId;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public String getFactoryId() { return factoryId; }
        public void setFactoryId(String factoryId) { this.factoryId = factoryId; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
