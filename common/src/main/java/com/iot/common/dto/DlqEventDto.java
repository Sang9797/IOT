package com.iot.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class DlqEventDto {

    private String eventId;
    private String traceId;
    private String schemaVersion;
    private String producerService;
    private String failedTopic;
    private String failedStage;
    private String pipeline;
    private String errorMessage;
    private String originalPayload;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime occurredAt;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getProducerService() { return producerService; }
    public void setProducerService(String producerService) { this.producerService = producerService; }
    public String getFailedTopic() { return failedTopic; }
    public void setFailedTopic(String failedTopic) { this.failedTopic = failedTopic; }
    public String getFailedStage() { return failedStage; }
    public void setFailedStage(String failedStage) { this.failedStage = failedStage; }
    public String getPipeline() { return pipeline; }
    public void setPipeline(String pipeline) { this.pipeline = pipeline; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getOriginalPayload() { return originalPayload; }
    public void setOriginalPayload(String originalPayload) { this.originalPayload = originalPayload; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
