package com.iot.devicemanagement.entity;

import com.iot.common.dto.DeviceDto;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "devices")
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceDto.DeviceType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceDto.DeviceStatus status;
    
    @Column(name = "factory_id")
    private String factoryId;
    
    private String location;
    
    @ElementCollection
    @CollectionTable(name = "device_configurations", joinColumns = @JoinColumn(name = "device_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> configuration;
    
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Device() {}

    public Device(String name, String address, DeviceDto.DeviceType type, DeviceDto.DeviceStatus status) {
        this.name = name;
        this.address = address;
        this.type = type;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DeviceDto.DeviceType getType() {
        return type;
    }

    public void setType(DeviceDto.DeviceType type) {
        this.type = type;
    }

    public DeviceDto.DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceDto.DeviceStatus status) {
        this.status = status;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Convert to DTO
    public DeviceDto toDto() {
        DeviceDto dto = new DeviceDto();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setAddress(this.address);
        dto.setType(this.type);
        dto.setStatus(this.status);
        dto.setFactoryId(this.factoryId);
        dto.setLocation(this.location);
        dto.setLastSeen(this.lastSeen);
        dto.setCreatedAt(this.createdAt);
        dto.setUpdatedAt(this.updatedAt);
        return dto;
    }

    // Update from DTO
    public void updateFromDto(DeviceDto dto) {
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getAddress() != null) this.address = dto.getAddress();
        if (dto.getType() != null) this.type = dto.getType();
        if (dto.getStatus() != null) this.status = dto.getStatus();
        if (dto.getFactoryId() != null) this.factoryId = dto.getFactoryId();
        if (dto.getLocation() != null) this.location = dto.getLocation();
        if (dto.getLastSeen() != null) this.lastSeen = dto.getLastSeen();
    }
}
