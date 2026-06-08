package com.iot.deviceprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import com.iot.common.config.KafkaHeaderNames;
import com.iot.common.config.KafkaTopics;
import com.iot.common.dto.CanonicalTelemetryEventDto;
import com.iot.common.dto.DeviceDataDto;
import com.iot.common.dto.DlqEventDto;
import com.iot.deviceprocessor.config.InfluxDbConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class DeviceDataService {

    private static final Logger log = LoggerFactory.getLogger(DeviceDataService.class);

    private static final String SCHEMA_VERSION = "1.0";
    private static final String PRODUCER_SERVICE = "device-processor-service";
    private static final String HIGH_THROUGHPUT = "high-throughput";

    private final InfluxDBClient influxDBClient;
    private final InfluxDbConfig influxDbConfig;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper mapper;

    private WriteApiBlocking writeApi;

    public DeviceDataService(
            InfluxDBClient influxDBClient,
            InfluxDbConfig influxDbConfig,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper mapper
    ) {
        this.influxDBClient = influxDBClient;
        this.influxDbConfig = influxDbConfig;
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
    }

    @KafkaListener(
            topics = KafkaTopics.MQTT_INGRESS,
            groupId = "device-processor-group",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    public void processMessage(String message) {
        String sanitizedMessage = message.replaceAll("^\"|\"$", "");
        String traceId = UUID.randomUUID().toString();
        log.info("Processing message from ingress topic {}: {}", KafkaTopics.MQTT_INGRESS, sanitizedMessage);

        try {
            String payload = decodeIngressPayload(sanitizedMessage);
            DeviceDataDto deviceData = mapper.readValue(payload, DeviceDataDto.class);

            CanonicalTelemetryEventDto rawEvent = buildTelemetryEvent(deviceData, traceId, "raw", KafkaTopics.MQTT_INGRESS);
            publishTelemetryEvent(KafkaTopics.DEVICE_DATA_RAW, rawEvent, "canonical-telemetry-raw");

            storeInTimeSeriesDB(deviceData);

            CanonicalTelemetryEventDto processedEvent = buildTelemetryEvent(
                    deviceData,
                    traceId,
                    "normalized",
                    KafkaTopics.DEVICE_DATA_RAW
            );
            publishTelemetryEvent(KafkaTopics.DEVICE_DATA_PROCESSED, processedEvent, "canonical-telemetry-normalized");
        } catch (Exception exception) {
            log.error("Failed to process ingress message", exception);
            publishDlqEvent(sanitizedMessage, traceId, "decode-or-normalize", exception);
        }
    }

    private String decodeIngressPayload(String payload) {
        return new String(Base64.getDecoder().decode(payload), StandardCharsets.UTF_8);
    }

    private void storeInTimeSeriesDB(DeviceDataDto deviceData) {
        if (writeApi == null) {
            writeApi = influxDBClient.getWriteApiBlocking();
        }

        Point point = Point.measurement("device_data")
                .addTag("device_id", deviceData.getDeviceId())
                .addTag("factory_id", deviceData.getFactoryId() != null ? deviceData.getFactoryId() : "unknown")
                .addTag("location", deviceData.getLocation() != null ? deviceData.getLocation() : "unknown")
                .time(Instant.now(), WritePrecision.MS);

        if (deviceData.getData() != null) {
            for (Map.Entry<String, Object> entry : deviceData.getData().entrySet()) {
                Object value = entry.getValue();
                switch (value) {
                    case Number n -> point.addField(entry.getKey(), n);
                    case String s -> point.addField(entry.getKey(), s);
                    case Boolean b -> point.addField(entry.getKey(), b);
                    default -> throw new IllegalStateException("Unexpected value: " + value);
                }
            }
        }

        if (deviceData.getBatteryLevel() != null) {
            point.addField("battery_level", deviceData.getBatteryLevel());
        }
        if (deviceData.getSignalStrength() != null) {
            point.addField("signal_strength", deviceData.getSignalStrength());
        }
        if (deviceData.getMessageType() != null) {
            point.addField("message_type", deviceData.getMessageType());
        }

        writeApi.writePoint(influxDbConfig.getBucket(), influxDbConfig.getOrg(), point);
        log.info("Stored device data in InfluxDB for device: {}", deviceData.getDeviceId());
    }

    private CanonicalTelemetryEventDto buildTelemetryEvent(DeviceDataDto deviceData, String traceId, String layer, String sourceTopic) {
        CanonicalTelemetryEventDto event = new CanonicalTelemetryEventDto();
        event.setEventId(UUID.randomUUID().toString());
        event.setTraceId(traceId);
        event.setSchemaVersion(SCHEMA_VERSION);
        event.setProducerService(PRODUCER_SERVICE);
        event.setSourceProtocol("MQTT");
        event.setSourceTopic(sourceTopic);
        event.setLayer(layer);
        event.setPipeline(HIGH_THROUGHPUT);
        event.setDeviceId(deviceData.getDeviceId());
        event.setFactoryId(deviceData.getFactoryId());
        event.setLocation(deviceData.getLocation());
        event.setMessageType(deviceData.getMessageType());
        event.setOccurredAt(deviceData.getTimestamp() != null ? deviceData.getTimestamp() : LocalDateTime.now());
        event.setIngestedAt(LocalDateTime.now());
        event.setPayload(deviceData);
        return event;
    }

    private void publishTelemetryEvent(String topic, CanonicalTelemetryEventDto event, String eventType) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                topic,
                event.getDeviceId(),
                event
        );
        RecordHeaders headers = (RecordHeaders) record.headers();
        headers.add(KafkaHeaderNames.EVENT_ID, event.getEventId().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.TRACE_ID, event.getTraceId().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.EVENT_TYPE, eventType.getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.PIPELINE, event.getPipeline().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.LAYER, event.getLayer().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.SCHEMA_VERSION, event.getSchemaVersion().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.PRODUCER_SERVICE, event.getProducerService().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.SOURCE_TOPIC, event.getSourceTopic().getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record);
    }

    private void publishDlqEvent(String originalPayload, String traceId, String failedStage, Exception exception) {
        DlqEventDto dlqEvent = new DlqEventDto();
        dlqEvent.setEventId(UUID.randomUUID().toString());
        dlqEvent.setTraceId(traceId);
        dlqEvent.setSchemaVersion(SCHEMA_VERSION);
        dlqEvent.setProducerService(PRODUCER_SERVICE);
        dlqEvent.setFailedTopic(KafkaTopics.MQTT_INGRESS);
        dlqEvent.setFailedStage(failedStage);
        dlqEvent.setPipeline(HIGH_THROUGHPUT);
        dlqEvent.setErrorMessage(exception.getMessage());
        dlqEvent.setOriginalPayload(originalPayload);
        dlqEvent.setOccurredAt(LocalDateTime.now());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.DEVICE_PROCESSING_DLQ,
                dlqEvent.getEventId(),
                dlqEvent
        );
        RecordHeaders headers = (RecordHeaders) record.headers();
        headers.add(KafkaHeaderNames.EVENT_ID, dlqEvent.getEventId().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.TRACE_ID, dlqEvent.getTraceId().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.EVENT_TYPE, "device-processing-dlq".getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.PIPELINE, dlqEvent.getPipeline().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.SCHEMA_VERSION, dlqEvent.getSchemaVersion().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.PRODUCER_SERVICE, dlqEvent.getProducerService().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.SOURCE_TOPIC, dlqEvent.getFailedTopic().getBytes(StandardCharsets.UTF_8));
        headers.add(KafkaHeaderNames.FAILED_STAGE, dlqEvent.getFailedStage().getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record);
    }
}
