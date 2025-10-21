package com.iot.devicemanagement.service;

import com.iot.common.dto.DeviceDto;
import com.iot.common.config.KafkaTopics;
import com.iot.devicemanagement.entity.Device;
import com.iot.devicemanagement.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeviceService {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public DeviceDto createDevice(DeviceDto deviceDto) {
        Device device = new Device();
        device.setName(deviceDto.getName());
        device.setAddress(deviceDto.getAddress());
        device.setType(deviceDto.getType());
        device.setStatus(deviceDto.getStatus());
        device.setFactoryId(deviceDto.getFactoryId());
        device.setLocation(deviceDto.getLocation());
        device.setLastSeen(LocalDateTime.now());
        
        Device savedDevice = deviceRepository.save(device);
        
        // Publish device metadata update
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
    
    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAll().stream()
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
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
        
        device.updateFromDto(deviceDto);
        Device updatedDevice = deviceRepository.save(device);
        
        // Publish device metadata update
        publishDeviceMetadataUpdate(updatedDevice);
        
        return updatedDevice.toDto();
    }
    
    public void updateDeviceStatus(String id, DeviceDto.DeviceStatus status) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
        
        DeviceDto.DeviceStatus oldStatus = device.getStatus();
        device.setStatus(status);
        device.setLastSeen(LocalDateTime.now());
        deviceRepository.save(device);
        
        // Publish status change if status actually changed
        if (!oldStatus.equals(status)) {
            publishDeviceStatusChange(device, oldStatus, status);
        }
    }
    
    public void updateLastSeen(String id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
        
        device.setLastSeen(LocalDateTime.now());
        deviceRepository.save(device);
    }
    
    public void deleteDevice(String id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
        
        deviceRepository.delete(device);
        
        // Publish device deletion
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
    
    private void publishDeviceMetadataUpdate(Device device) {
        DeviceDto deviceDto = device.toDto();
        kafkaTemplate.send(KafkaTopics.DEVICE_METADATA_UPDATES, device.getId(), deviceDto);
    }
    
    private void publishDeviceStatusChange(Device device, DeviceDto.DeviceStatus oldStatus, DeviceDto.DeviceStatus newStatus) {
        DeviceStatusChangeEvent event = new DeviceStatusChangeEvent(
                device.getId(),
                device.getName(),
                device.getFactoryId(),
                oldStatus,
                newStatus,
                LocalDateTime.now()
        );
        kafkaTemplate.send(KafkaTopics.DEVICE_STATUS_CHANGES, device.getId(), event);
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
    
    // Event classes
    public static class DeviceStatusChangeEvent {
        private String deviceId;
        private String deviceName;
        private String factoryId;
        private DeviceDto.DeviceStatus oldStatus;
        private DeviceDto.DeviceStatus newStatus;
        private LocalDateTime timestamp;
        
        public DeviceStatusChangeEvent() {}
        
        public DeviceStatusChangeEvent(String deviceId, String deviceName, String factoryId, 
                                     DeviceDto.DeviceStatus oldStatus, DeviceDto.DeviceStatus newStatus, 
                                     LocalDateTime timestamp) {
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.factoryId = factoryId;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public String getFactoryId() { return factoryId; }
        public void setFactoryId(String factoryId) { this.factoryId = factoryId; }
        public DeviceDto.DeviceStatus getOldStatus() { return oldStatus; }
        public void setOldStatus(DeviceDto.DeviceStatus oldStatus) { this.oldStatus = oldStatus; }
        public DeviceDto.DeviceStatus getNewStatus() { return newStatus; }
        public void setNewStatus(DeviceDto.DeviceStatus newStatus) { this.newStatus = newStatus; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
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
