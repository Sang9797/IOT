package com.iot.common.config;

public final class KafkaHeaderNames {

    public static final String EVENT_ID = "event_id";
    public static final String TRACE_ID = "trace_id";
    public static final String EVENT_TYPE = "event_type";
    public static final String PIPELINE = "pipeline";
    public static final String LAYER = "layer";
    public static final String SCHEMA_VERSION = "schema_version";
    public static final String PRODUCER_SERVICE = "producer_service";
    public static final String SOURCE_TOPIC = "source_topic";
    public static final String FAILED_STAGE = "failed_stage";
    public static final String MQTT_BROKER_ID = "mqtt_broker_id";
    public static final String FACTORY_ID = "factory_id";
    public static final String SOURCE_BRIDGE_ID = "source_bridge_id";
    public static final String DEVICE_ID = "device_id";

    private KafkaHeaderNames() {
    }
}
