package com.iot.notification.controller;

import com.iot.common.dto.NotificationDto;
import com.iot.notification.handler.NotificationWebSocketHandler;
import com.iot.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationWebSocketHandler webSocketHandler;
    
    @PostMapping
    public ResponseEntity<Map<String, String>> sendNotification(@Valid @RequestBody NotificationDto notification) {
        try {
            notificationService.sendCustomNotification(notification);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification sent successfully");
            response.put("notificationId", notification.getNotificationId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send notification: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeWebSocketConnections", webSocketHandler.getActiveConnections());
        stats.put("service", "notification-service");
        stats.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "notification-service");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        health.put("activeConnections", String.valueOf(webSocketHandler.getActiveConnections()));
        
        return ResponseEntity.ok(health);
    }
}
