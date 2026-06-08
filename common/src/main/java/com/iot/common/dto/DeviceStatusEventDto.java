package com.iot.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class DeviceStatusEventDto {

    private String eventId;
    private String traceId;
    private String schemaVersion;
    private String producerService;
    private String pipeline;

    private String deviceId;
    private String deviceName;
    private String factoryId;
    private DeviceDto.DeviceStatus previousStatus;
    private DeviceDto.DeviceStatus currentStatus;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime changedAt;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getProducerService() {
        return producerService;
    }

    public void setProducerService(String producerService) {
        this.producerService = producerService;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public DeviceDto.DeviceStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(DeviceDto.DeviceStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public DeviceDto.DeviceStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(DeviceDto.DeviceStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
