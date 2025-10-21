package com.iot.analysisreport.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import com.iot.analysisreport.config.InfluxDbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {
    
    @Autowired
    private InfluxDBClient influxDBClient;
    
    @Autowired
    private InfluxDbConfig influxDbConfig;
    
    private QueryApi queryApi;
    
    public Map<String, Object> generateDeviceReport(String deviceId, int hours) {
        try {
            if (queryApi == null) {
                queryApi = influxDBClient.getQueryApi();
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);
            
            String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                "|> range(start: %s, stop: %s) " +
                "|> filter(fn: (r) => r._measurement == \"device_data\") " +
                "|> filter(fn: (r) => r.device_id == \"%s\") " +
                "|> aggregateWindow(every: 1h, fn: mean, createEmpty: false)",
                influxDbConfig.getBucket(),
                startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                deviceId
            );
            
            List<FluxTable> tables = queryApi.query(fluxQuery);
            
            Map<String, Object> report = new HashMap<>();
            report.put("deviceId", deviceId);
            report.put("reportPeriod", hours + " hours");
            report.put("generatedAt", LocalDateTime.now());
            report.put("data", processFluxTables(tables));
            
            return report;
            
        } catch (Exception e) {
            System.err.println("Error generating device report: " + e.getMessage());
            return createErrorReport(deviceId, e.getMessage());
        }
    }
    
    public Map<String, Object> generateFactoryReport(String factoryId, int hours) {
        try {
            if (queryApi == null) {
                queryApi = influxDBClient.getQueryApi();
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);
            
            String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                "|> range(start: %s, stop: %s) " +
                "|> filter(fn: (r) => r._measurement == \"device_data\") " +
                "|> filter(fn: (r) => r.factory_id == \"%s\") " +
                "|> group(columns: [\"device_id\"]) " +
                "|> aggregateWindow(every: 1h, fn: mean, createEmpty: false)",
                influxDbConfig.getBucket(),
                startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                factoryId
            );
            
            List<FluxTable> tables = queryApi.query(fluxQuery);
            
            Map<String, Object> report = new HashMap<>();
            report.put("factoryId", factoryId);
            report.put("reportPeriod", hours + " hours");
            report.put("generatedAt", LocalDateTime.now());
            report.put("data", processFluxTables(tables));
            
            return report;
            
        } catch (Exception e) {
            System.err.println("Error generating factory report: " + e.getMessage());
            return createErrorReport(factoryId, e.getMessage());
        }
    }
    
    public Map<String, Object> generateAnomalyReport(int hours) {
        try {
            if (queryApi == null) {
                queryApi = influxDBClient.getQueryApi();
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);
            
            String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                "|> range(start: %s, stop: %s) " +
                "|> filter(fn: (r) => r._measurement == \"device_data\") " +
                "|> filter(fn: (r) => r._field == \"anomaly_detected\") " +
                "|> filter(fn: (r) => r._value == true) " +
                "|> group(columns: [\"device_id\", \"factory_id\"]) " +
                "|> count()",
                influxDbConfig.getBucket(),
                startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            List<FluxTable> tables = queryApi.query(fluxQuery);
            
            Map<String, Object> report = new HashMap<>();
            report.put("reportType", "anomaly");
            report.put("reportPeriod", hours + " hours");
            report.put("generatedAt", LocalDateTime.now());
            report.put("anomalies", processFluxTables(tables));
            
            return report;
            
        } catch (Exception e) {
            System.err.println("Error generating anomaly report: " + e.getMessage());
            return createErrorReport("anomaly", e.getMessage());
        }
    }
    
    public Map<String, Object> generatePerformanceReport(String deviceId, int hours) {
        try {
            if (queryApi == null) {
                queryApi = influxDBClient.getQueryApi();
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);
            
            // Get performance metrics
            String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                "|> range(start: %s, stop: %s) " +
                "|> filter(fn: (r) => r._measurement == \"device_data\") " +
                "|> filter(fn: (r) => r.device_id == \"%s\") " +
                "|> filter(fn: (r) => r._field =~ /temperature|pressure|vibration|battery_level/) " +
                "|> group(columns: [\"_field\"]) " +
                "|> aggregateWindow(every: 1h, fn: [mean, max, min], createEmpty: false)",
                influxDbConfig.getBucket(),
                startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                deviceId
            );
            
            List<FluxTable> tables = queryApi.query(fluxQuery);
            
            Map<String, Object> report = new HashMap<>();
            report.put("deviceId", deviceId);
            report.put("reportType", "performance");
            report.put("reportPeriod", hours + " hours");
            report.put("generatedAt", LocalDateTime.now());
            report.put("metrics", processFluxTables(tables));
            
            return report;
            
        } catch (Exception e) {
            System.err.println("Error generating performance report: " + e.getMessage());
            return createErrorReport(deviceId, e.getMessage());
        }
    }
    
    private List<Map<String, Object>> processFluxTables(List<FluxTable> tables) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (FluxTable table : tables) {
            table.getRecords().forEach(record -> {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("time", record.getTime());
                dataPoint.put("measurement", record.getMeasurement());
                dataPoint.put("field", record.getField());
                dataPoint.put("value", record.getValue());
                
                // Add tags
                record.getValues().forEach((key, value) -> {
                    if (key.startsWith("_") && !key.equals("_time") && !key.equals("_measurement") && 
                        !key.equals("_field") && !key.equals("_value")) {
                        dataPoint.put(key.substring(1), value);
                    }
                });
                
                result.add(dataPoint);
            });
        }
        
        return result;
    }
    
    private Map<String, Object> createErrorReport(String identifier, String error) {
        Map<String, Object> errorReport = new HashMap<>();
        errorReport.put("error", true);
        errorReport.put("identifier", identifier);
        errorReport.put("message", error);
        errorReport.put("generatedAt", LocalDateTime.now());
        return errorReport;
    }
}
