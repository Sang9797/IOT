package com.iot.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class CanonicalTelemetryEventDto {

    private String eventId;
    private String traceId;
    private String schemaVersion;
    private String producerService;
    private String sourceProtocol;
    private String sourceTopic;
    private String layer;
    private String pipeline;

    private String deviceId;
    private String factoryId;
    private String location;
    private String messageType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime occurredAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ingestedAt;

    private DeviceDataDto payload;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getProducerService() { return producerService; }
    public void setProducerService(String producerService) { this.producerService = producerService; }
    public String getSourceProtocol() { return sourceProtocol; }
    public void setSourceProtocol(String sourceProtocol) { this.sourceProtocol = sourceProtocol; }
    public String getSourceTopic() { return sourceTopic; }
    public void setSourceTopic(String sourceTopic) { this.sourceTopic = sourceTopic; }
    public String getLayer() { return layer; }
    public void setLayer(String layer) { this.layer = layer; }
    public String getPipeline() { return pipeline; }
    public void setPipeline(String pipeline) { this.pipeline = pipeline; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getFactoryId() { return factoryId; }
    public void setFactoryId(String factoryId) { this.factoryId = factoryId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public LocalDateTime getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(LocalDateTime ingestedAt) { this.ingestedAt = ingestedAt; }
    public DeviceDataDto getPayload() { return payload; }
    public void setPayload(DeviceDataDto payload) { this.payload = payload; }
}
