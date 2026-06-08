CREATE TABLE mqtt_brokers (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    protocol VARCHAR(50) NOT NULL,
    username VARCHAR(255),
    password VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    topic_prefix VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE devices
    ADD COLUMN mqtt_broker_id VARCHAR(255);

ALTER TABLE devices
    ADD CONSTRAINT fk_devices_mqtt_broker
        FOREIGN KEY (mqtt_broker_id) REFERENCES mqtt_brokers(id);

CREATE INDEX idx_devices_mqtt_broker_id ON devices(mqtt_broker_id);
