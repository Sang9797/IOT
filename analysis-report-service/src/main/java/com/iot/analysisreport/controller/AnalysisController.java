package com.iot.analysisreport.controller;

import com.iot.analysisreport.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {
    
    @Autowired
    private ReportService reportService;
    
    @GetMapping("/devices/{deviceId}/report")
    public ResponseEntity<Map<String, Object>> getDeviceReport(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "24") int hours) {
        
        try {
            Map<String, Object> report = reportService.generateDeviceReport(deviceId, hours);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/factories/{factoryId}/report")
    public ResponseEntity<Map<String, Object>> getFactoryReport(
            @PathVariable String factoryId,
            @RequestParam(defaultValue = "24") int hours) {
        
        try {
            Map<String, Object> report = reportService.generateFactoryReport(factoryId, hours);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/anomalies/report")
    public ResponseEntity<Map<String, Object>> getAnomalyReport(
            @RequestParam(defaultValue = "24") int hours) {
        
        try {
            Map<String, Object> report = reportService.generateAnomalyReport(hours);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/devices/{deviceId}/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceReport(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "24") int hours) {
        
        try {
            Map<String, Object> report = reportService.generatePerformanceReport(deviceId, hours);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = Map.of(
                "status", "UP",
                "service", "analysis-report-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
        
        return ResponseEntity.ok(health);
    }
}
