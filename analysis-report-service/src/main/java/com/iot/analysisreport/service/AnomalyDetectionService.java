package com.iot.analysisreport.service;

import com.iot.common.config.KafkaHeaderNames;
import com.iot.common.config.KafkaMessageFactory;
import com.iot.common.config.KafkaTopics;
import com.iot.common.dto.AlertDto;
import com.iot.common.dto.CanonicalTelemetryEventDto;
import com.iot.common.dto.DeviceDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnomalyDetectionService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final Map<String, List<DeviceDataDto>> deviceDataHistory = new ConcurrentHashMap<>();
    private final int MAX_HISTORY_SIZE = 100;

    @KafkaListener(topics = KafkaTopics.DEVICE_DATA_PROCESSED, groupId = "analysis-group")
    public void analyzeDeviceData(CanonicalTelemetryEventDto telemetryEvent) {
        try {
            DeviceDataDto deviceData = telemetryEvent.getPayload();
            if (deviceData == null) {
                return;
            }

            System.out.println("Analyzing device data for device: " + deviceData.getDeviceId());
            storeDataInHistory(deviceData);
            detectStatisticalAnomalies(deviceData, telemetryEvent.getTraceId());
            detectTrendAnomalies(deviceData, telemetryEvent.getTraceId());
            detectPatternAnomalies(deviceData, telemetryEvent.getTraceId());
        } catch (Exception e) {
            System.err.println("Error analyzing device data: " + e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.DEVICE_ANOMALIES, groupId = "analysis-group")
    public void processAnomalyEvent(Map<String, Object> anomalyEvent) {
        try {
            System.out.println("Processing anomaly event: " + anomalyEvent);
            AlertDto alert = createAlertFromAnomaly(anomalyEvent);
            publishAlert(alert, UUID.randomUUID().toString());
        } catch (Exception e) {
            System.err.println("Error processing anomaly event: " + e.getMessage());
        }
    }

    private void storeDataInHistory(DeviceDataDto deviceData) {
        deviceDataHistory.computeIfAbsent(deviceData.getDeviceId(), key -> new ArrayList<>()).add(deviceData);
        List<DeviceDataDto> history = deviceDataHistory.get(deviceData.getDeviceId());
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    private void detectStatisticalAnomalies(DeviceDataDto deviceData, String traceId) {
        List<DeviceDataDto> history = deviceDataHistory.get(deviceData.getDeviceId());
        if (history == null || history.size() < 10) {
            return;
        }

        Map<String, Object> currentData = deviceData.getData();
        if (currentData == null) {
            return;
        }

        for (String key : currentData.keySet()) {
            Object value = currentData.get(key);
            if (!(value instanceof Number)) {
                continue;
            }

            double currentValue = ((Number) value).doubleValue();
            List<Double> values = history.stream()
                    .map(data -> data.getData().get(key))
                    .filter(Objects::nonNull)
                    .filter(v -> v instanceof Number)
                    .map(v -> ((Number) v).doubleValue())
                    .toList();

            if (values.size() < 5) {
                continue;
            }

            double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = values.stream()
                    .mapToDouble(v -> Math.pow(v - mean, 2))
                    .average().orElse(0.0);
            double stdDev = Math.sqrt(variance);

            if (stdDev > 0 && Math.abs(currentValue - mean) > 3 * stdDev) {
                createStatisticalAnomalyAlert(deviceData, key, currentValue, mean, stdDev, traceId);
            }
        }
    }

    private void detectTrendAnomalies(DeviceDataDto deviceData, String traceId) {
        List<DeviceDataDto> history = deviceDataHistory.get(deviceData.getDeviceId());
        if (history == null || history.size() < 20) {
            return;
        }

        Map<String, Object> currentData = deviceData.getData();
        if (currentData == null) {
            return;
        }

        for (String key : currentData.keySet()) {
            Object value = currentData.get(key);
            if (!(value instanceof Number)) {
                continue;
            }

            double currentValue = ((Number) value).doubleValue();
            List<Double> recentValues = history.stream()
                    .skip(Math.max(0, history.size() - 10))
                    .map(data -> data.getData().get(key))
                    .filter(Objects::nonNull)
                    .filter(v -> v instanceof Number)
                    .map(v -> ((Number) v).doubleValue())
                    .toList();

            if (recentValues.size() < 5) {
                continue;
            }

            double trend = calculateTrend(recentValues);
            if (Math.abs(trend) > 0.5) {
                createTrendAnomalyAlert(deviceData, key, currentValue, trend, traceId);
            }
        }
    }

    private void detectPatternAnomalies(DeviceDataDto deviceData, String traceId) {
        Map<String, Object> currentData = deviceData.getData();
        if (currentData == null) {
            return;
        }

        if (currentData.containsKey("temperature") && currentData.containsKey("pressure")) {
            Object tempObj = currentData.get("temperature");
            Object pressureObj = currentData.get("pressure");

            if (tempObj instanceof Number && pressureObj instanceof Number) {
                double temperature = ((Number) tempObj).doubleValue();
                double pressure = ((Number) pressureObj).doubleValue();

                if (temperature > 70 && pressure < 2.0) {
                    createPatternAnomalyAlert(
                            deviceData,
                            "High temperature with low pressure",
                            traceId,
                            Map.of("temperature", temperature, "pressure", pressure)
                    );
                }
            }
        }
    }

    private double calculateTrend(List<Double> values) {
        if (values.size() < 2) {
            return 0.0;
        }

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXX = 0;
        int n = values.size();

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumXX += i * i;
        }

        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }

    private void createStatisticalAnomalyAlert(DeviceDataDto deviceData, String parameter,
                                               double currentValue, double mean, double stdDev, String traceId) {
        AlertDto alert = new AlertDto();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setDeviceId(deviceData.getDeviceId());
        alert.setAlertType(AlertDto.AlertType.PERFORMANCE_DEGRADATION);
        alert.setSeverity(AlertDto.Severity.HIGH);
        alert.setMessage(String.format(
                "Statistical anomaly detected in %s: current=%.2f, mean=%.2f, stdDev=%.2f",
                parameter,
                currentValue,
                mean,
                stdDev
        ));
        alert.setTimestamp(LocalDateTime.now());
        alert.setFactoryId(deviceData.getFactoryId());
        alert.setLocation(deviceData.getLocation());
        alert.setData(Map.of(
                "parameter", parameter,
                "currentValue", currentValue,
                "mean", mean,
                "stdDev", stdDev,
                "anomalyType", "STATISTICAL"
        ));

        publishAlert(alert, traceId);
    }

    private void createTrendAnomalyAlert(DeviceDataDto deviceData, String parameter,
                                         double currentValue, double trend, String traceId) {
        AlertDto alert = new AlertDto();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setDeviceId(deviceData.getDeviceId());
        alert.setAlertType(AlertDto.AlertType.PERFORMANCE_DEGRADATION);
        alert.setSeverity(AlertDto.Severity.MEDIUM);
        alert.setMessage(String.format(
                "Trend anomaly detected in %s: current=%.2f, trend=%.2f",
                parameter,
                currentValue,
                trend
        ));
        alert.setTimestamp(LocalDateTime.now());
        alert.setFactoryId(deviceData.getFactoryId());
        alert.setLocation(deviceData.getLocation());
        alert.setData(Map.of(
                "parameter", parameter,
                "currentValue", currentValue,
                "trend", trend,
                "anomalyType", "TREND"
        ));

        publishAlert(alert, traceId);
    }

    private void createPatternAnomalyAlert(DeviceDataDto deviceData, String pattern, String traceId, Map<String, Object> data) {
        AlertDto alert = new AlertDto();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setDeviceId(deviceData.getDeviceId());
        alert.setAlertType(AlertDto.AlertType.PERFORMANCE_DEGRADATION);
        alert.setSeverity(AlertDto.Severity.HIGH);
        alert.setMessage("Pattern anomaly detected: " + pattern);
        alert.setTimestamp(LocalDateTime.now());
        alert.setFactoryId(deviceData.getFactoryId());
        alert.setLocation(deviceData.getLocation());
        alert.setData(data);

        publishAlert(alert, traceId);
    }

    private AlertDto createAlertFromAnomaly(Map<String, Object> anomalyEvent) {
        AlertDto alert = new AlertDto();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setDeviceId((String) anomalyEvent.get("deviceId"));
        alert.setAlertType(AlertDto.AlertType.PERFORMANCE_DEGRADATION);
        alert.setSeverity(AlertDto.Severity.HIGH);
        alert.setMessage("Anomaly detected: " + anomalyEvent.get("details"));
        alert.setTimestamp(LocalDateTime.now());
        alert.setFactoryId((String) anomalyEvent.get("factoryId"));
        alert.setData((Map<String, Object>) anomalyEvent.get("data"));
        return alert;
    }

    private void publishAlert(AlertDto alert, String traceId) {
        kafkaTemplate.send(KafkaMessageFactory.buildMessage(
                KafkaTopics.DEVICE_ALERTS,
                alert.getDeviceId(),
                alert,
                Map.of(
                        KafkaHeaderNames.EVENT_ID, alert.getAlertId(),
                        KafkaHeaderNames.TRACE_ID, traceId,
                        KafkaHeaderNames.EVENT_TYPE, "device-alert-event",
                        KafkaHeaderNames.PIPELINE, "high-integrity",
                        KafkaHeaderNames.SCHEMA_VERSION, "1.0",
                        KafkaHeaderNames.PRODUCER_SERVICE, "analysis-report-service"
                )
        ));
    }
}
