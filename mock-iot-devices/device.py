"""
Mock IoT Device implementation
"""

import json
import time
import random
import logging
from datetime import datetime, timedelta
from typing import Dict, Any, Optional
import paho.mqtt.client as mqtt
import requests
from faker import Faker

from config import (
    MQTT_BROKER_HOST, MQTT_BROKER_PORT, MQTT_USERNAME, MQTT_PASSWORD,
    API_BASE_URL, DEVICE_TYPES, ANOMALY_PROBABILITY, BATTERY_DRAIN_RATE
)

fake = Faker()
logger = logging.getLogger(__name__)


class MockIoTDevice:
    """Mock IoT Device that simulates real device behavior"""
    
    def __init__(self, device_id: str, device_type: str, factory_id: str, location: str):
        self.device_id = device_id
        self.device_type = device_type
        self.factory_id = factory_id
        self.location = location
        self.status = "ONLINE"
        self.battery_level = 100.0
        self.signal_strength = -50
        self.last_seen = datetime.now()
        
        # Device configuration
        self.config = DEVICE_TYPES.get(device_type, DEVICE_TYPES['TEMPERATURE_SENSOR'])
        self.sampling_rate = self.config['sampling_rate']
        
        # MQTT client
        self.mqtt_client = mqtt.Client()
        self.mqtt_client.on_connect = self._on_mqtt_connect
        self.mqtt_client.on_message = self._on_mqtt_message
        self.mqtt_client.on_disconnect = self._on_mqtt_disconnect
        
        # Device state
        self.is_running = False
        self.data_counter = 0
        self.anomaly_count = 0
        
        # Base values for data generation
        self._initialize_base_values()
        
    def _initialize_base_values(self):
        """Initialize base values for data generation"""
        if self.device_type == 'TEMPERATURE_SENSOR':
            self.base_value = self.config['base_temperature']
            self.value_range = self.config['temperature_range']
        elif self.device_type == 'PRESSURE_SENSOR':
            self.base_value = self.config['base_pressure']
            self.value_range = self.config['pressure_range']
        elif self.device_type == 'VIBRATION_MONITOR':
            self.base_value = self.config['base_vibration']
            self.value_range = self.config['vibration_range']
        elif self.device_type == 'CONTROL_VALVE':
            self.base_value = self.config['base_position']
            self.value_range = self.config['position_range']
        else:
            self.base_value = 25.0
            self.value_range = (0, 100)
    
    def _on_mqtt_connect(self, client, userdata, flags, rc):
        """MQTT connection callback"""
        if rc == 0:
            logger.info(f"Device {self.device_id} connected to MQTT broker")
            # Subscribe to control commands
            client.subscribe(f"devices/{self.device_id}/control")
            client.subscribe("devices/all/control")
        else:
            logger.error(f"Device {self.device_id} failed to connect to MQTT broker: {rc}")
    
    def _on_mqtt_message(self, client, userdata, msg):
        """MQTT message callback"""
        try:
            topic = msg.topic
            payload = json.loads(msg.payload.decode())
            
            logger.info(f"Device {self.device_id} received command: {payload}")
            
            # Process control command
            self._process_control_command(payload)
            
        except Exception as e:
            logger.error(f"Error processing MQTT message: {e}")
    
    def _on_mqtt_disconnect(self, client, userdata, rc):
        """MQTT disconnection callback"""
        logger.warning(f"Device {self.device_id} disconnected from MQTT broker")
    
    def _process_control_command(self, command: Dict[str, Any]):
        """Process control commands received via MQTT"""
        command_type = command.get('commandType', '')
        payload = command.get('payload', '')
        
        response = {
            'deviceId': self.device_id,
            'commandId': command.get('commandId', ''),
            'commandType': command_type,
            'status': 'success',
            'timestamp': datetime.now().isoformat(),
            'response': {}
        }
        
        if command_type == 'START':
            self.start()
            response['response'] = {'message': 'Device started successfully'}
        elif command_type == 'STOP':
            self.stop()
            response['response'] = {'message': 'Device stopped successfully'}
        elif command_type == 'RESTART':
            self.restart()
            response['response'] = {'message': 'Device restarted successfully'}
        elif command_type == 'STATUS_CHECK':
            response['response'] = {
                'status': self.status,
                'batteryLevel': self.battery_level,
                'signalStrength': self.signal_strength,
                'lastSeen': self.last_seen.isoformat()
            }
        elif command_type == 'EMERGENCY_STOP':
            self.emergency_stop()
            response['response'] = {'message': 'Emergency stop executed'}
        elif command_type == 'CONFIGURE':
            self.configure(payload)
            response['response'] = {'message': 'Configuration updated'}
        else:
            response['status'] = 'error'
            response['response'] = {'message': f'Unknown command: {command_type}'}
        
        # Send response back
        self._send_mqtt_response(response)
    
    def _send_mqtt_response(self, response: Dict[str, Any]):
        """Send response back via MQTT"""
        topic = f"devices/{self.device_id}/control"
        self.mqtt_client.publish(topic, json.dumps(response))
        logger.info(f"Device {self.device_id} sent response: {response}")
    
    def connect(self):
        """Connect to MQTT broker"""
        try:
            if MQTT_USERNAME and MQTT_PASSWORD:
                self.mqtt_client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
            
            self.mqtt_client.connect(MQTT_BROKER_HOST, MQTT_BROKER_PORT, 60)
            self.mqtt_client.loop_start()
            logger.info(f"Device {self.device_id} connecting to MQTT broker...")
            return True
        except Exception as e:
            logger.error(f"Failed to connect device {self.device_id}: {e}")
            return False
    
    def disconnect(self):
        """Disconnect from MQTT broker"""
        self.is_running = False
        self.mqtt_client.loop_stop()
        self.mqtt_client.disconnect()
        logger.info(f"Device {self.device_id} disconnected")
    
    def start(self):
        """Start device operation"""
        self.is_running = True
        self.status = "ONLINE"
        logger.info(f"Device {self.device_id} started")
    
    def stop(self):
        """Stop device operation"""
        self.is_running = False
        self.status = "OFFLINE"
        logger.info(f"Device {self.device_id} stopped")
    
    def restart(self):
        """Restart device"""
        self.stop()
        time.sleep(2)
        self.start()
        logger.info(f"Device {self.device_id} restarted")
    
    def emergency_stop(self):
        """Emergency stop device"""
        self.is_running = False
        self.status = "ERROR"
        logger.warning(f"Device {self.device_id} emergency stop executed")
    
    def configure(self, config_data: str):
        """Configure device"""
        try:
            config = json.loads(config_data)
            if 'sampling_rate' in config:
                self.sampling_rate = config['sampling_rate']
            logger.info(f"Device {self.device_id} configured: {config}")
        except Exception as e:
            logger.error(f"Failed to configure device {self.device_id}: {e}")
    
    def generate_sensor_data(self) -> Dict[str, Any]:
        """Generate realistic sensor data"""
        if not self.is_running:
            return None
        
        # Generate base sensor value with some noise
        noise = random.uniform(-2, 2)
        sensor_value = self.base_value + noise
        
        # Add some drift over time
        drift = random.uniform(-0.1, 0.1)
        sensor_value += drift
        
        # Ensure value is within range
        sensor_value = max(self.value_range[0], min(self.value_range[1], sensor_value))
        
        # Check for anomalies
        is_anomaly = False
        if random.random() < ANOMALY_PROBABILITY:
            is_anomaly = True
            self.anomaly_count += 1
            # Generate anomaly value
            if sensor_value > self.config['anomaly_threshold']:
                sensor_value = random.uniform(self.config['anomaly_threshold'], self.value_range[1])
            else:
                sensor_value = random.uniform(self.value_range[0], -self.config['anomaly_threshold'])
        
        # Update battery level (drain over time)
        self.battery_level = max(0, self.battery_level - BATTERY_DRAIN_RATE / 3600)
        
        # Update signal strength (random variation)
        self.signal_strength = random.uniform(-80, -30)
        
        # Update last seen
        self.last_seen = datetime.now()
        
        # Create data payload
        data = {
            'deviceId': self.device_id,
            'timestamp': self.last_seen.isoformat(),
            'factoryId': self.factory_id,
            'location': self.location,
            'messageType': 'sensor_data',
            'batteryLevel': round(self.battery_level, 2),
            'signalStrength': int(self.signal_strength),
            'data': {
                'value': round(sensor_value, 2),
                'unit': self._get_unit(),
                'isAnomaly': is_anomaly,
                'anomalyCount': self.anomaly_count
            }
        }
        
        # Add device-specific data
        if self.device_type == 'TEMPERATURE_SENSOR':
            data['data']['temperature'] = sensor_value
            data['data']['humidity'] = random.uniform(30, 70)
        elif self.device_type == 'PRESSURE_SENSOR':
            data['data']['pressure'] = sensor_value
            data['data']['flow_rate'] = random.uniform(0, 100)
        elif self.device_type == 'VIBRATION_MONITOR':
            data['data']['vibration'] = sensor_value
            data['data']['frequency'] = random.uniform(10, 1000)
        elif self.device_type == 'CONTROL_VALVE':
            data['data']['position'] = sensor_value
            data['data']['flow_rate'] = random.uniform(0, 100)
        
        return data
    
    def _get_unit(self) -> str:
        """Get unit for the sensor type"""
        units = {
            'TEMPERATURE_SENSOR': '°C',
            'PRESSURE_SENSOR': 'bar',
            'VIBRATION_MONITOR': 'g',
            'CONTROL_VALVE': '%'
        }
        return units.get(self.device_type, 'units')
    
    def send_data(self, data: Dict[str, Any]):
        """Send sensor data via MQTT"""
        if not self.is_running:
            return
        
        topic = f"devices/{self.device_id}/data"
        payload = json.dumps(data)
        
        try:
            self.mqtt_client.publish(topic, payload)
            self.data_counter += 1
            logger.debug(f"Device {self.device_id} sent data: {data['data']['value']} {self._get_unit()}")
        except Exception as e:
            logger.error(f"Failed to send data from device {self.device_id}: {e}")
    
    def send_status(self):
        """Send device status update"""
        status_data = {
            'deviceId': self.device_id,
            'timestamp': datetime.now().isoformat(),
            'status': self.status,
            'batteryLevel': self.battery_level,
            'signalStrength': self.signal_strength,
            'lastSeen': self.last_seen.isoformat(),
            'dataCounter': self.data_counter,
            'anomalyCount': self.anomaly_count
        }
        
        topic = f"devices/{self.device_id}/status"
        payload = json.dumps(status_data)
        
        try:
            self.mqtt_client.publish(topic, payload)
            logger.debug(f"Device {self.device_id} sent status update")
        except Exception as e:
            logger.error(f"Failed to send status from device {self.device_id}: {e}")
    
    def run(self):
        """Main device loop"""
        logger.info(f"Device {self.device_id} starting main loop")
        
        while True:
            try:
                if self.is_running:
                    # Generate and send sensor data
                    data = self.generate_sensor_data()
                    if data:
                        self.send_data(data)
                    
                    # Send status update every 10 data points
                    if self.data_counter % 10 == 0:
                        self.send_status()
                
                # Sleep for sampling rate
                time.sleep(self.sampling_rate)
                
            except KeyboardInterrupt:
                logger.info(f"Device {self.device_id} received interrupt signal")
                break
            except Exception as e:
                logger.error(f"Error in device {self.device_id} main loop: {e}")
                time.sleep(5)  # Wait before retrying
        
        self.disconnect()
        logger.info(f"Device {self.device_id} main loop ended")
    
    def get_info(self) -> Dict[str, Any]:
        """Get device information"""
        return {
            'deviceId': self.device_id,
            'deviceType': self.device_type,
            'factoryId': self.factory_id,
            'location': self.location,
            'status': self.status,
            'batteryLevel': self.battery_level,
            'signalStrength': self.signal_strength,
            'lastSeen': self.last_seen.isoformat(),
            'dataCounter': self.data_counter,
            'anomalyCount': self.anomaly_count,
            'isRunning': self.is_running,
            'samplingRate': self.sampling_rate
        }
