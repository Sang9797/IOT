package com.iot.devicemanagement.controller;

import com.iot.common.dto.DeviceDto;
import com.iot.devicemanagement.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/devices")
@CrossOrigin(origins = "*")
public class DeviceController {
    
    @Autowired
    private DeviceService deviceService;
    
    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody DeviceDto deviceDto) {
        try {
            DeviceDto createdDevice = deviceService.createDevice(deviceDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable String id) {
        Optional<DeviceDto> device = deviceService.getDeviceById(id);
        return device.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/address/{address}")
    public ResponseEntity<DeviceDto> getDeviceByAddress(@PathVariable String address) {
        Optional<DeviceDto> device = deviceService.getDeviceByAddress(address);
        return device.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<DeviceDto>> getAllDevices() {
        List<DeviceDto> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/factory/{factoryId}")
    public ResponseEntity<List<DeviceDto>> getDevicesByFactory(@PathVariable String factoryId) {
        List<DeviceDto> devices = deviceService.getDevicesByFactory(factoryId);
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeviceDto>> getDevicesByStatus(@PathVariable DeviceDto.DeviceStatus status) {
        List<DeviceDto> devices = deviceService.getDevicesByStatus(status);
        return ResponseEntity.ok(devices);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable String id, 
                                                 @Valid @RequestBody DeviceDto deviceDto) {
        try {
            DeviceDto updatedDevice = deviceService.updateDevice(id, deviceDto);
            return ResponseEntity.ok(updatedDevice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateDeviceStatus(@PathVariable String id, 
                                                  @RequestParam DeviceDto.DeviceStatus status) {
        try {
            deviceService.updateDeviceStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/last-seen")
    public ResponseEntity<Void> updateLastSeen(@PathVariable String id) {
        try {
            deviceService.updateLastSeen(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) {
        try {
            deviceService.deleteDevice(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/offline")
    public ResponseEntity<List<DeviceDto>> getOfflineDevices(@RequestParam(defaultValue = "30") int minutesThreshold) {
        List<DeviceDto> offlineDevices = deviceService.getOfflineDevices(minutesThreshold);
        return ResponseEntity.ok(offlineDevices);
    }
    
    @GetMapping("/stats/count")
    public ResponseEntity<DeviceStats> getDeviceStats(@RequestParam(required = false) String factoryId) {
        DeviceStats stats = new DeviceStats();
        
        if (factoryId != null) {
            stats.setTotalDevices(deviceService.getDeviceCountByFactory(factoryId));
            stats.setOnlineDevices(deviceService.getDeviceCountByStatus(DeviceDto.DeviceStatus.ONLINE));
            stats.setOfflineDevices(deviceService.getDeviceCountByStatus(DeviceDto.DeviceStatus.OFFLINE));
        } else {
            stats.setTotalDevices(deviceService.getAllDevices().size());
            stats.setOnlineDevices(deviceService.getDeviceCountByStatus(DeviceDto.DeviceStatus.ONLINE));
            stats.setOfflineDevices(deviceService.getDeviceCountByStatus(DeviceDto.DeviceStatus.OFFLINE));
        }
        
        return ResponseEntity.ok(stats);
    }
    
    // Statistics DTO
    public static class DeviceStats {
        private long totalDevices;
        private long onlineDevices;
        private long offlineDevices;
        
        public DeviceStats() {}
        
        public long getTotalDevices() { return totalDevices; }
        public void setTotalDevices(long totalDevices) { this.totalDevices = totalDevices; }
        public long getOnlineDevices() { return onlineDevices; }
        public void setOnlineDevices(long onlineDevices) { this.onlineDevices = onlineDevices; }
        public long getOfflineDevices() { return offlineDevices; }
        public void setOfflineDevices(long offlineDevices) { this.offlineDevices = offlineDevices; }
    }
}
