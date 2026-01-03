package com.iot.deviceprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.iot.common.dto.DeviceDataDto;
import com.iot.deviceprocessor.config.InfluxDbConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceDataService {

    private final InfluxDBClient influxDBClient;

    private final InfluxDbConfig influxDbConfig;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper mapper;

    private WriteApiBlocking writeApi;


    @KafkaListener(topics = "mqtt-messages", groupId = "device-processor-group")
    public void processMessage(String message) {
        message = message.replaceAll("^\"|\"$", "");
        log.info("Processing message: {}", message);
        try {
            String payload = new String(Base64.getDecoder().decode(message));
            log.info("Processing payload: {}", payload);
            DeviceDataDto deviceData = mapper.readValue(payload, DeviceDataDto.class);

            // Store in InfluxDB
            storeInTimeSeriesDB(deviceData);

        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
    }

    private void storeInTimeSeriesDB(DeviceDataDto deviceData) {
        try {
            if (writeApi == null) {
                writeApi = influxDBClient.getWriteApiBlocking();
            }

            // Create a point for InfluxDB
            Point point = Point.measurement("device_data")
                    .addTag("device_id", deviceData.getDeviceId())
                    .addTag("factory_id", deviceData.getFactoryId() != null ? deviceData.getFactoryId() : "unknown")
                    .addTag("location", deviceData.getLocation() != null ? deviceData.getLocation() : "unknown")
                    .time(Instant.now(), WritePrecision.MS);

            // Add all data fields as fields
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

            // Add metadata fields
            if (deviceData.getBatteryLevel() != null) {
                point.addField("battery_level", deviceData.getBatteryLevel());
            }
            if (deviceData.getSignalStrength() != null) {
                point.addField("signal_strength", deviceData.getSignalStrength());
            }
            if (deviceData.getMessageType() != null) {
                point.addField("message_type", deviceData.getMessageType());
            }

            // Write to InfluxDB
            writeApi.writePoint(influxDbConfig.getBucket(), influxDbConfig.getOrg(), point);

            log.info("Stored device data in InfluxDB for device: {}", deviceData.getDeviceId());

        } catch (Exception e) {
            log.error("Error storing data in InfluxDB: {}", e.getMessage());
        }
    }
}
