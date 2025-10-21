package com.iot.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ControlCommandDto {
    
    @NotBlank(message = "Command ID is required")
    private String commandId;
    
    @NotNull(message = "Command type is required")
    private CommandType commandType;
    
    @NotBlank(message = "Command payload is required")
    private String payload;
    
    private List<String> deviceIds; // For specific devices
    private boolean broadcastToAll; // For all devices
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String factoryId;
    private String userId;
    private Map<String, Object> parameters;
    private Integer priority;
    private Integer timeoutSeconds;

    // Constructors
    public ControlCommandDto() {}

    public ControlCommandDto(String commandId, CommandType commandType, String payload) {
        this.commandId = commandId;
        this.commandType = commandType;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public boolean isBroadcastToAll() {
        return broadcastToAll;
    }

    public void setBroadcastToAll(boolean broadcastToAll) {
        this.broadcastToAll = broadcastToAll;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public enum CommandType {
        START, STOP, RESTART, CONFIGURE, STATUS_CHECK, EMERGENCY_STOP, MAINTENANCE_MODE
    }
}
