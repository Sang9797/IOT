package com.iot.deviceprocessor.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.iot.common.config.KafkaTopics;
import com.iot.common.dto.DeviceDataDto;
import com.iot.deviceprocessor.config.InfluxDbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class DeviceDataService {
    
    @Autowired
    private InfluxDBClient influxDBClient;
    
    @Autowired
    private InfluxDbConfig influxDbConfig;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private WriteApiBlocking writeApi;
    
    @KafkaListener(topics = KafkaTopics.DEVICE_DATA_RAW, groupId = "device-processor-group")
    public void processDeviceData(DeviceDataDto deviceData) {
        try {
            System.out.println("Processing device data for device: " + deviceData.getDeviceId());
            
            // Store in InfluxDB
            storeInTimeSeriesDB(deviceData);
            
            // Publish processed data for analysis
            kafkaTemplate.send(KafkaTopics.DEVICE_DATA_PROCESSED, deviceData.getDeviceId(), deviceData);
            
            // Check for anomalies (basic threshold checking)
            checkForAnomalies(deviceData);
            
        } catch (Exception e) {
            System.err.println("Error processing device data: " + e.getMessage());
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
                    if (value instanceof Number) {
                        point.addField(entry.getKey(), (Number) value);
                    } else if (value instanceof String) {
                        point.addField(entry.getKey(), (String) value);
                    } else if (value instanceof Boolean) {
                        point.addField(entry.getKey(), (Boolean) value);
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
            
            System.out.println("Stored device data in InfluxDB for device: " + deviceData.getDeviceId());
            
        } catch (Exception e) {
            System.err.println("Error storing data in InfluxDB: " + e.getMessage());
        }
    }
    
    private void checkForAnomalies(DeviceDataDto deviceData) {
        try {
            if (deviceData.getData() == null) return;
            
            Map<String, Object> data = deviceData.getData();
            boolean hasAnomaly = false;
            StringBuilder anomalyDetails = new StringBuilder();
            
            // Check temperature anomalies
            if (data.containsKey("temperature")) {
                Object tempObj = data.get("temperature");
                if (tempObj instanceof Number) {
                    double temperature = ((Number) tempObj).doubleValue();
                    if (temperature > 80.0 || temperature < -10.0) {
                        hasAnomaly = true;
                        anomalyDetails.append("Temperature anomaly: ").append(temperature).append("°C. ");
                    }
                }
            }
            
            // Check pressure anomalies
            if (data.containsKey("pressure")) {
                Object pressureObj = data.get("pressure");
                if (pressureObj instanceof Number) {
                    double pressure = ((Number) pressureObj).doubleValue();
                    if (pressure > 10.0 || pressure < 0.1) {
                        hasAnomaly = true;
                        anomalyDetails.append("Pressure anomaly: ").append(pressure).append(" bar. ");
                    }
                }
            }
            
            // Check vibration anomalies
            if (data.containsKey("vibration")) {
                Object vibrationObj = data.get("vibration");
                if (vibrationObj instanceof Number) {
                    double vibration = ((Number) vibrationObj).doubleValue();
                    if (vibration > 5.0) {
                        hasAnomaly = true;
                        anomalyDetails.append("Vibration anomaly: ").append(vibration).append(" g. ");
                    }
                }
            }
            
            // Check battery level
            if (deviceData.getBatteryLevel() != null && deviceData.getBatteryLevel() < 20.0) {
                hasAnomaly = true;
                anomalyDetails.append("Low battery: ").append(deviceData.getBatteryLevel()).append("%. ");
            }
            
            // Check signal strength
            if (deviceData.getSignalStrength() != null && deviceData.getSignalStrength() < -80) {
                hasAnomaly = true;
                anomalyDetails.append("Weak signal: ").append(deviceData.getSignalStrength()).append(" dBm. ");
            }
            
            if (hasAnomaly) {
                // Create anomaly event
                Map<String, Object> anomalyEvent = Map.of(
                    "deviceId", deviceData.getDeviceId(),
                    "timestamp", deviceData.getTimestamp(),
                    "anomalyType", "THRESHOLD_EXCEEDED",
                    "details", anomalyDetails.toString(),
                    "data", data,
                    "factoryId", deviceData.getFactoryId() != null ? deviceData.getFactoryId() : "unknown"
                );
                
                // Publish anomaly to Kafka
                kafkaTemplate.send(KafkaTopics.DEVICE_ANOMALIES, deviceData.getDeviceId(), anomalyEvent);
                
                System.out.println("Anomaly detected for device " + deviceData.getDeviceId() + ": " + anomalyDetails);
            }
            
        } catch (Exception e) {
            System.err.println("Error checking for anomalies: " + e.getMessage());
        }
    }
}
