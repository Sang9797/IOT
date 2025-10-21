package com.iot.notification.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.common.dto.NotificationDto;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler implements WebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        System.out.println("WebSocket connection established: " + sessionId);
        
        // Send welcome message
        sendMessage(session, Map.of(
            "type", "connection",
            "message", "Connected to notification service",
            "sessionId", sessionId
        ));
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = (String) message.getPayload();
        System.out.println("Received WebSocket message: " + payload);
        
        try {
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String messageType = (String) messageData.get("type");
            
            switch (messageType) {
                case "subscribe":
                    handleSubscription(session, messageData);
                    break;
                case "unsubscribe":
                    handleUnsubscription(session, messageData);
                    break;
                case "ping":
                    sendMessage(session, Map.of("type", "pong", "timestamp", System.currentTimeMillis()));
                    break;
                default:
                    sendMessage(session, Map.of("type", "error", "message", "Unknown message type: " + messageType));
            }
        } catch (Exception e) {
            sendMessage(session, Map.of("type", "error", "message", "Invalid message format"));
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error: " + exception.getMessage());
        sessions.remove(session.getId());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        System.out.println("WebSocket connection closed: " + sessionId + ", status: " + closeStatus);
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    public void broadcastNotification(NotificationDto notification) {
        Map<String, Object> message = Map.of(
            "type", "notification",
            "notification", notification,
            "timestamp", System.currentTimeMillis()
        );
        
        broadcastMessage(message);
    }
    
    public void broadcastAlert(String deviceId, String alertMessage, String severity) {
        Map<String, Object> message = Map.of(
            "type", "alert",
            "deviceId", deviceId,
            "message", alertMessage,
            "severity", severity,
            "timestamp", System.currentTimeMillis()
        );
        
        broadcastMessage(message);
    }
    
    public void broadcastDeviceStatusUpdate(String deviceId, String status) {
        Map<String, Object> message = Map.of(
            "type", "device_status",
            "deviceId", deviceId,
            "status", status,
            "timestamp", System.currentTimeMillis()
        );
        
        broadcastMessage(message);
    }
    
    private void broadcastMessage(Map<String, Object> message) {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            System.err.println("Error serializing message: " + e.getMessage());
            return;
        }
        
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            } catch (IOException e) {
                System.err.println("Error sending message to session " + session.getId() + ": " + e.getMessage());
                sessions.remove(session.getId());
            }
        });
    }
    
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    
    private void handleSubscription(WebSocketSession session, Map<String, Object> messageData) {
        String subscriptionType = (String) messageData.get("subscriptionType");
        String deviceId = (String) messageData.get("deviceId");
        String factoryId = (String) messageData.get("factoryId");
        
        // Store subscription preferences in session attributes
        session.getAttributes().put("subscriptionType", subscriptionType);
        session.getAttributes().put("deviceId", deviceId);
        session.getAttributes().put("factoryId", factoryId);
        
        sendMessage(session, Map.of(
            "type", "subscription_confirmed",
            "subscriptionType", subscriptionType,
            "deviceId", deviceId,
            "factoryId", factoryId
        ));
    }
    
    private void handleUnsubscription(WebSocketSession session, Map<String, Object> messageData) {
        session.getAttributes().remove("subscriptionType");
        session.getAttributes().remove("deviceId");
        session.getAttributes().remove("factoryId");
        
        sendMessage(session, Map.of(
            "type", "unsubscription_confirmed",
            "message", "Unsubscribed from notifications"
        ));
    }
    
    public int getActiveConnections() {
        return sessions.size();
    }
}
