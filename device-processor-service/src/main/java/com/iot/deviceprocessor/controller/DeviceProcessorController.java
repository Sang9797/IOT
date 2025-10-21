package com.iot.deviceprocessor.controller;

import com.iot.common.dto.ControlCommandDto;
import com.iot.deviceprocessor.service.MqttService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/processor")
@CrossOrigin(origins = "*")
public class DeviceProcessorController {
    
    @Autowired
    private MqttService mqttService;
    
    @PostMapping("/devices/{deviceId}/control")
    public ResponseEntity<Map<String, String>> sendControlCommand(
            @PathVariable String deviceId,
            @Valid @RequestBody ControlCommandDto command) {
        
        try {
            // Convert command to JSON string
            String commandJson = String.format(
                "{\"commandId\":\"%s\",\"commandType\":\"%s\",\"payload\":\"%s\",\"timestamp\":\"%s\"}",
                command.getCommandId(),
                command.getCommandType(),
                command.getPayload(),
                command.getTimestamp()
            );
            
            mqttService.publishControlCommand(deviceId, commandJson);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Control command sent to device " + deviceId);
            response.put("commandId", command.getCommandId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send control command: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/devices/all/control")
    public ResponseEntity<Map<String, String>> sendBroadcastCommand(
            @Valid @RequestBody ControlCommandDto command) {
        
        try {
            // Convert command to JSON string
            String commandJson = String.format(
                "{\"commandId\":\"%s\",\"commandType\":\"%s\",\"payload\":\"%s\",\"timestamp\":\"%s\"}",
                command.getCommandId(),
                command.getCommandType(),
                command.getPayload(),
                command.getTimestamp()
            );
            
            mqttService.publishBroadcastCommand(commandJson);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Broadcast control command sent to all devices");
            response.put("commandId", command.getCommandId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send broadcast command: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "device-processor-service");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("service", "device-processor-service");
        stats.put("status", "running");
        stats.put("timestamp", java.time.LocalDateTime.now().toString());
        stats.put("mqtt_connected", true); // This should be checked from MqttService
        
        return ResponseEntity.ok(stats);
    }
}
