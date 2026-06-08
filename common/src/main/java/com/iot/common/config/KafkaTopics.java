package com.iot.common.config;

public class KafkaTopics {

    // Topic layers
    public static final String DEVICE_DATA_RAW_HIGH_THROUGHPUT = "device.data.raw.high-throughput";
    public static final String DEVICE_DATA_PROCESSED_HIGH_THROUGHPUT = "device.data.processed.high-throughput";
    public static final String DEVICE_STATUS_CHANGES_HIGH_INTEGRITY = "device.status.changes.high-integrity";
    public static final String DEVICE_ALERTS_HIGH_INTEGRITY = "device.alerts.high-integrity";
    public static final String DEVICE_PROCESSING_DLQ = "device.processing.dlq";

    // Device Management Topics
    public static final String DEVICE_METADATA_UPDATES = "device.metadata.updates";
    public static final String DEVICE_STATUS_CHANGES = DEVICE_STATUS_CHANGES_HIGH_INTEGRITY;

    // Device Data Topics
    public static final String DEVICE_DATA_RAW = DEVICE_DATA_RAW_HIGH_THROUGHPUT;
    public static final String DEVICE_DATA_PROCESSED = DEVICE_DATA_PROCESSED_HIGH_THROUGHPUT;
    public static final String DEVICE_HEALTH_CHECK = "device.health.check";

    // Control Command Topics
    public static final String DEVICE_CONTROL_COMMANDS = "device.control.commands.high-integrity";
    public static final String DEVICE_COMMAND_RESPONSES = "device.command.responses.high-integrity";

    // Analysis and Alert Topics
    public static final String DEVICE_ANALYSIS_RESULTS = "device.analysis.results.high-throughput";
    public static final String DEVICE_ALERTS = DEVICE_ALERTS_HIGH_INTEGRITY;
    public static final String DEVICE_ANOMALIES = "device.anomalies.high-throughput";

    // Notification Topics
    public static final String NOTIFICATION_REQUESTS = "notification.requests.high-integrity";
    public static final String NOTIFICATION_STATUS = "notification.status.high-integrity";

    // MQTT Bridge Topics
    public static final String MQTT_BRIDGE_DATA = "mqtt.bridge.data";
    public static final String MQTT_BRIDGE_COMMANDS = "mqtt.bridge.commands";
    public static final String MQTT_INGRESS = "mqtt-messages";
    public static final String MQTT_BROKER_UPDATES = "mqtt.broker.updates";

    private KafkaTopics() {
        // Utility class
    }
}
