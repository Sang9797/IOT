package com.iot.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public class AlertDto {
    
    @NotBlank(message = "Alert ID is required")
    private String alertId;
    
    @NotBlank(message = "Device ID is required")
    private String deviceId;
    
    @NotNull(message = "Alert type is required")
    private AlertType alertType;
    
    @NotNull(message = "Severity is required")
    private Severity severity;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String factoryId;
    private String location;
    private Map<String, Object> data;
    private boolean acknowledged;
    private String acknowledgedBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime acknowledgedAt;

    // Constructors
    public AlertDto() {}

    public AlertDto(String alertId, String deviceId, AlertType alertType, Severity severity, String message) {
        this.alertId = alertId;
        this.deviceId = deviceId;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public enum AlertType {
        TEMPERATURE_HIGH, TEMPERATURE_LOW, PRESSURE_HIGH, PRESSURE_LOW,
        VIBRATION_ANOMALY, POWER_FAILURE, COMMUNICATION_LOST, MAINTENANCE_DUE,
        PERFORMANCE_DEGRADATION, SECURITY_BREACH
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
