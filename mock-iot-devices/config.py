"""
Configuration settings for Mock IoT Devices
"""

import os
from dotenv import load_dotenv

load_dotenv()

# MQTT Configuration
MQTT_BROKER_HOST = os.getenv('MQTT_BROKER_HOST', 'localhost')
MQTT_BROKER_PORT = int(os.getenv('MQTT_BROKER_PORT', 1883))
MQTT_USERNAME = os.getenv('MQTT_USERNAME', '')
MQTT_PASSWORD = os.getenv('MQTT_PASSWORD', '')

# API Configuration
API_GATEWAY_URL = os.getenv('API_GATEWAY_URL', 'http://localhost:8080')
API_BASE_URL = f"{API_GATEWAY_URL}/api"

# Device Configuration
DEFAULT_FACTORY_ID = os.getenv('DEFAULT_FACTORY_ID', 'factory-001')
DEFAULT_LOCATION = os.getenv('DEFAULT_LOCATION', 'Production Line A')
DEVICE_COUNT = int(os.getenv('DEVICE_COUNT', 10))

# Data Generation Settings
DATA_INTERVAL = int(os.getenv('DATA_INTERVAL', 5))  # seconds
ANOMALY_PROBABILITY = float(os.getenv('ANOMALY_PROBABILITY', 0.05))  # 5% chance
BATTERY_DRAIN_RATE = float(os.getenv('BATTERY_DRAIN_RATE', 0.1))  # per hour

# Device Types and their configurations
DEVICE_TYPES = {
    'TEMPERATURE_SENSOR': {
        'base_temperature': 25.0,
        'temperature_range': (-10, 80),
        'anomaly_threshold': 70,
        'sampling_rate': 5
    },
    'PRESSURE_SENSOR': {
        'base_pressure': 2.0,
        'pressure_range': (0.1, 10.0),
        'anomaly_threshold': 8.0,
        'sampling_rate': 10
    },
    'VIBRATION_MONITOR': {
        'base_vibration': 0.5,
        'vibration_range': (0.0, 5.0),
        'anomaly_threshold': 4.0,
        'sampling_rate': 1
    },
    'CONTROL_VALVE': {
        'base_position': 50,
        'position_range': (0, 100),
        'anomaly_threshold': 90,
        'sampling_rate': 30
    }
}

# Factory configurations
FACTORIES = {
    'factory-001': {
        'name': 'Main Production Facility',
        'location': 'Building A',
        'device_count': 50
    },
    'factory-002': {
        'name': 'Secondary Production Facility', 
        'location': 'Building B',
        'device_count': 30
    },
    'factory-003': {
        'name': 'Quality Control Facility',
        'location': 'Building C',
        'device_count': 20
    }
}

# Logging Configuration
LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')
LOG_FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
