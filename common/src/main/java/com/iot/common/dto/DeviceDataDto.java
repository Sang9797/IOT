package com.iot.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class DeviceDataDto {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull(message = "Data payload is required")
    private Map<String, Object> data;

    private String factoryId;
    private String location;
    private String messageType;
    private Double batteryLevel;
    private Integer signalStrength;

    // Constructors
    public DeviceDataDto() {
    }

    public DeviceDataDto(String deviceId, LocalDateTime timestamp, Map<String, Object> data) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.data = data;
    }
}
