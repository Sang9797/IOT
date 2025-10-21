package com.iot.notification.service;

import com.iot.common.config.KafkaTopics;
import com.iot.common.dto.AlertDto;
import com.iot.common.dto.NotificationDto;
import com.iot.notification.handler.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationWebSocketHandler webSocketHandler;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private SmsService smsService;
    
    @KafkaListener(topics = KafkaTopics.DEVICE_ALERTS, groupId = "notification-group")
    public void processAlert(AlertDto alert) {
        try {
            System.out.println("Processing alert: " + alert.getAlertId());
            
            // Create notification from alert
            NotificationDto notification = createNotificationFromAlert(alert);
            
            // Send notifications based on severity
            sendNotifications(notification, alert.getSeverity());
            
            // Broadcast to WebSocket clients
            webSocketHandler.broadcastNotification(notification);
            
            // Publish notification status
            publishNotificationStatus(notification, "sent");
            
        } catch (Exception e) {
            System.err.println("Error processing alert: " + e.getMessage());
        }
    }
    
    @KafkaListener(topics = KafkaTopics.DEVICE_STATUS_CHANGES, groupId = "notification-group")
    public void processDeviceStatusChange(Map<String, Object> statusChange) {
        try {
            System.out.println("Processing device status change: " + statusChange);
            
            String deviceId = (String) statusChange.get("deviceId");
            String newStatus = (String) statusChange.get("newStatus");
            
            // Broadcast status change to WebSocket clients
            webSocketHandler.broadcastDeviceStatusUpdate(deviceId, newStatus);
            
            // Create notification for critical status changes
            if ("OFFLINE".equals(newStatus) || "ERROR".equals(newStatus)) {
                NotificationDto notification = createStatusChangeNotification(deviceId, newStatus);
                sendNotifications(notification, AlertDto.Severity.HIGH);
                webSocketHandler.broadcastNotification(notification);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing device status change: " + e.getMessage());
        }
    }
    
    private NotificationDto createNotificationFromAlert(AlertDto alert) {
        NotificationDto notification = new NotificationDto();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setType(NotificationDto.NotificationType.DASHBOARD);
        notification.setTitle("Device Alert: " + alert.getAlertType());
        notification.setMessage(alert.getMessage());
        notification.setRecipients(getRecipientsForAlert(alert));
        notification.setTimestamp(LocalDateTime.now());
        notification.setAlertId(alert.getAlertId());
        notification.setDeviceId(alert.getDeviceId());
        notification.setFactoryId(alert.getFactoryId());
        notification.setMetadata(Map.of(
            "alertType", alert.getAlertType(),
            "severity", alert.getSeverity(),
            "data", alert.getData()
        ));
        
        return notification;
    }
    
    private NotificationDto createStatusChangeNotification(String deviceId, String status) {
        NotificationDto notification = new NotificationDto();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setType(NotificationDto.NotificationType.DASHBOARD);
        notification.setTitle("Device Status Change");
        notification.setMessage("Device " + deviceId + " status changed to " + status);
        notification.setRecipients(getDefaultRecipients());
        notification.setTimestamp(LocalDateTime.now());
        notification.setDeviceId(deviceId);
        notification.setMetadata(Map.of("status", status));
        
        return notification;
    }
    
    private void sendNotifications(NotificationDto notification, AlertDto.Severity severity) {
        List<String> recipients = notification.getRecipients();
        
        // Always send dashboard notification
        sendDashboardNotification(notification);
        
        // Send email for medium and high severity
        if (severity == AlertDto.Severity.MEDIUM || severity == AlertDto.Severity.HIGH || severity == AlertDto.Severity.CRITICAL) {
            sendEmailNotification(notification);
        }
        
        // Send SMS for high and critical severity
        if (severity == AlertDto.Severity.HIGH || severity == AlertDto.Severity.CRITICAL) {
            sendSmsNotification(notification);
        }
    }
    
    private void sendDashboardNotification(NotificationDto notification) {
        // Dashboard notification is handled by WebSocket broadcast
        System.out.println("Dashboard notification sent: " + notification.getNotificationId());
    }
    
    private void sendEmailNotification(NotificationDto notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipients().toArray(new String[0]));
            message.setSubject(notification.getTitle());
            message.setText(notification.getMessage());
            message.setFrom("noreply@iot-system.com");
            
            mailSender.send(message);
            System.out.println("Email notification sent: " + notification.getNotificationId());
            
        } catch (Exception e) {
            System.err.println("Error sending email notification: " + e.getMessage());
        }
    }
    
    private void sendSmsNotification(NotificationDto notification) {
        try {
            for (String recipient : notification.getRecipients()) {
                smsService.sendSms(recipient, notification.getMessage());
            }
            System.out.println("SMS notification sent: " + notification.getNotificationId());
            
        } catch (Exception e) {
            System.err.println("Error sending SMS notification: " + e.getMessage());
        }
    }
    
    private List<String> getRecipientsForAlert(AlertDto alert) {
        // In a real system, this would query a user management service
        // For now, return default recipients
        return getDefaultRecipients();
    }
    
    private List<String> getDefaultRecipients() {
        // In a real system, this would be configurable
        return Arrays.asList("admin@factory.com", "manager@factory.com");
    }
    
    private void publishNotificationStatus(NotificationDto notification, String status) {
        Map<String, Object> statusUpdate = Map.of(
            "notificationId", notification.getNotificationId(),
            "status", status,
            "timestamp", LocalDateTime.now()
        );
        
        kafkaTemplate.send(KafkaTopics.NOTIFICATION_STATUS, notification.getNotificationId(), statusUpdate);
    }
    
    public void sendCustomNotification(NotificationDto notification) {
        try {
            sendNotifications(notification, AlertDto.Severity.MEDIUM);
            webSocketHandler.broadcastNotification(notification);
            publishNotificationStatus(notification, "sent");
            
        } catch (Exception e) {
            System.err.println("Error sending custom notification: " + e.getMessage());
            publishNotificationStatus(notification, "failed");
        }
    }
}
