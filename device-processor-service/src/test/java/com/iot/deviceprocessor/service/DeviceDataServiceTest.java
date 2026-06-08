package com.iot.deviceprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.iot.common.config.KafkaTopics;
import com.iot.common.config.KafkaHeaderNames;
import com.iot.common.dto.CanonicalTelemetryEventDto;
import com.iot.common.dto.DeviceDataDto;
import com.iot.common.dto.DlqEventDto;
import com.iot.deviceprocessor.config.InfluxDbConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceDataServiceTest {

    @Mock
    private InfluxDBClient influxDBClient;

    @Mock
    private InfluxDbConfig influxDbConfig;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private WriteApiBlocking writeApiBlocking;

    private DeviceDataService deviceDataService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        deviceDataService = new DeviceDataService(influxDBClient, influxDbConfig, kafkaTemplate, objectMapper);
    }

    @Test
    void processMessagePublishesRawAndNormalizedTelemetry() throws Exception {
        when(influxDBClient.getWriteApiBlocking()).thenReturn(writeApiBlocking);
        when(influxDbConfig.getBucket()).thenReturn("iot-data");
        when(influxDbConfig.getOrg()).thenReturn("iot-org");

        DeviceDataDto deviceData = new DeviceDataDto();
        deviceData.setDeviceId("device-001");
        deviceData.setTimestamp(LocalDateTime.of(2026, 6, 8, 10, 15, 0));
        deviceData.setFactoryId("factory-001");
        deviceData.setLocation("line-a");
        deviceData.setMessageType("sensor_data");
        deviceData.setBatteryLevel(95.2);
        deviceData.setSignalStrength(-45);
        deviceData.setData(Map.of("temperature", 25.5, "humidity", 45.2));

        String encodedPayload = Base64.getEncoder()
                .encodeToString(objectMapper.writeValueAsString(deviceData).getBytes(StandardCharsets.UTF_8));

        ConsumerRecord<String, String> record = buildIngressRecord("device-001", encodedPayload, "broker-1", "factory-001", "mqtt-bridge-broker-1", "factory-a/sensors/temp");
        deviceDataService.processMessage(record);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(2)).send(recordCaptor.capture());
        verify(writeApiBlocking, times(1)).writePoint(any(String.class), any(String.class), any());

        List<ProducerRecord<String, Object>> records = recordCaptor.getAllValues();
        CanonicalTelemetryEventDto rawEvent = assertTelemetryRecord(records.get(0), KafkaTopics.DEVICE_DATA_RAW, "raw");
        CanonicalTelemetryEventDto normalizedEvent = assertTelemetryRecord(records.get(1), KafkaTopics.DEVICE_DATA_PROCESSED, "normalized");

        assertEquals("device-001", rawEvent.getDeviceId());
        assertEquals("device-001", normalizedEvent.getDeviceId());
        assertEquals(rawEvent.getTraceId(), normalizedEvent.getTraceId());
        assertEquals("broker-1", rawEvent.getMqttBrokerId());
        assertEquals("mqtt-bridge-broker-1", rawEvent.getSourceBridgeId());
    }

    @Test
    void processMessagePublishesDlqWhenPayloadCannotBeDecoded() {
        deviceDataService.processMessage(buildIngressRecord("device-001", "not-base64", "broker-1", "factory-001", "mqtt-bridge-broker-1", "factory-a/sensors/temp"));

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());
        verify(influxDBClient, never()).getWriteApiBlocking();

        ProducerRecord<String, Object> record = recordCaptor.getValue();
        assertEquals(KafkaTopics.DEVICE_PROCESSING_DLQ, record.topic());
        DlqEventDto dlqEvent = assertInstanceOf(DlqEventDto.class, record.value());
        assertEquals("decode-or-normalize", dlqEvent.getFailedStage());
        assertEquals("factory-a/sensors/temp", dlqEvent.getFailedTopic());
    }

    private ConsumerRecord<String, String> buildIngressRecord(
            String key,
            String value,
            String mqttBrokerId,
            String factoryId,
            String sourceBridgeId,
            String sourceTopic
    ) {
        ConsumerRecord<String, String> record = new ConsumerRecord<>(KafkaTopics.MQTT_INGRESS, 0, 0L, key, value);
        record.headers().add(KafkaHeaderNames.MQTT_BROKER_ID, mqttBrokerId.getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaderNames.FACTORY_ID, factoryId.getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaderNames.SOURCE_BRIDGE_ID, sourceBridgeId.getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaderNames.SOURCE_TOPIC, sourceTopic.getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaderNames.DEVICE_ID, key.getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaderNames.TRACE_ID, "trace-1".getBytes(StandardCharsets.UTF_8));
        return record;
    }

    @SuppressWarnings("unchecked")
    private CanonicalTelemetryEventDto assertTelemetryRecord(
            ProducerRecord<String, Object> record,
            String expectedTopic,
            String expectedLayer
    ) {
        assertEquals(expectedTopic, record.topic());
        CanonicalTelemetryEventDto telemetryEvent = assertInstanceOf(CanonicalTelemetryEventDto.class, record.value());
        assertEquals(expectedLayer, telemetryEvent.getLayer());
        assertEquals("high-throughput", telemetryEvent.getPipeline());
        return telemetryEvent;
    }
}
