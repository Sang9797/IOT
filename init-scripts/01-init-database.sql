-- Initialize IoT Devices Database

-- Create database if not exists
CREATE DATABASE iot_devices;

-- Use the database
\c iot_devices;

-- Create devices table
CREATE TABLE IF NOT EXISTS devices (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    factory_id VARCHAR(255),
    location VARCHAR(255),
    last_seen TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create device configurations table
CREATE TABLE IF NOT EXISTS device_configurations (
    device_id VARCHAR(255) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    PRIMARY KEY (device_id, config_key),
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_devices_factory_id ON devices(factory_id);
CREATE INDEX IF NOT EXISTS idx_devices_status ON devices(status);
CREATE INDEX IF NOT EXISTS idx_devices_type ON devices(type);
CREATE INDEX IF NOT EXISTS idx_devices_last_seen ON devices(last_seen);

-- Insert sample data
INSERT INTO devices (id, name, address, type, status, factory_id, location) VALUES
('device-001', 'Temperature Sensor 1', '192.168.1.100', 'SENSOR', 'ONLINE', 'factory-001', 'Production Line A'),
('device-002', 'Pressure Sensor 1', '192.168.1.101', 'SENSOR', 'ONLINE', 'factory-001', 'Production Line A'),
('device-003', 'Vibration Monitor 1', '192.168.1.102', 'MONITOR', 'ONLINE', 'factory-001', 'Production Line B'),
('device-004', 'Control Valve 1', '192.168.1.103', 'ACTUATOR', 'ONLINE', 'factory-001', 'Production Line B'),
('device-005', 'Temperature Sensor 2', '192.168.1.104', 'SENSOR', 'OFFLINE', 'factory-002', 'Production Line C')
ON CONFLICT (id) DO NOTHING;

-- Insert sample configurations
INSERT INTO device_configurations (device_id, config_key, config_value) VALUES
('device-001', 'sampling_rate', '5'),
('device-001', 'threshold_high', '80'),
('device-001', 'threshold_low', '-10'),
('device-002', 'sampling_rate', '10'),
('device-002', 'threshold_high', '10'),
('device-002', 'threshold_low', '0.1'),
('device-003', 'sampling_rate', '1'),
('device-003', 'threshold_high', '5'),
('device-004', 'response_time', '2'),
('device-004', 'max_pressure', '15'),
('device-005', 'sampling_rate', '5'),
('device-005', 'threshold_high', '80'),
('device-005', 'threshold_low', '-10')
ON CONFLICT (device_id, config_key) DO NOTHING;
