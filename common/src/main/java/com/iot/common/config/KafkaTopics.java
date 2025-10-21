package com.iot.common.config;

public class KafkaTopics {
    
    // Device Management Topics
    public static final String DEVICE_METADATA_UPDATES = "device.metadata.updates";
    public static final String DEVICE_STATUS_CHANGES = "device.status.changes";
    
    // Device Data Topics
    public static final String DEVICE_DATA_RAW = "device.data.raw";
    public static final String DEVICE_DATA_PROCESSED = "device.data.processed";
    public static final String DEVICE_HEALTH_CHECK = "device.health.check";
    
    // Control Command Topics
    public static final String DEVICE_CONTROL_COMMANDS = "device.control.commands";
    public static final String DEVICE_COMMAND_RESPONSES = "device.command.responses";
    
    // Analysis and Alert Topics
    public static final String DEVICE_ANALYSIS_RESULTS = "device.analysis.results";
    public static final String DEVICE_ALERTS = "device.alerts";
    public static final String DEVICE_ANOMALIES = "device.anomalies";
    
    // Notification Topics
    public static final String NOTIFICATION_REQUESTS = "notification.requests";
    public static final String NOTIFICATION_STATUS = "notification.status";
    
    // MQTT Bridge Topics
    public static final String MQTT_BRIDGE_DATA = "mqtt.bridge.data";
    public static final String MQTT_BRIDGE_COMMANDS = "mqtt.bridge.commands";
    
    private KafkaTopics() {
        // Utility class
    }
}
