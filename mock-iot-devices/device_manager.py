"""
Device Manager for managing multiple mock IoT devices
"""

import json
import time
import logging
import threading
from typing import Dict, List, Optional
import requests
from faker import Faker

from config import (
    API_BASE_URL, DEFAULT_FACTORY_ID, DEFAULT_LOCATION, DEVICE_COUNT,
    DEVICE_TYPES, FACTORIES
)
from device import MockIoTDevice

fake = Faker()
logger = logging.getLogger(__name__)


class DeviceManager:
    """Manages multiple mock IoT devices"""
    
    def __init__(self):
        self.devices: Dict[str, MockIoTDevice] = {}
        self.device_threads: Dict[str, threading.Thread] = {}
        self.is_running = False
        self.registered_devices = []
        
    def create_devices(self, count: int = None, factory_id: str = None) -> List[MockIoTDevice]:
        """Create multiple mock devices"""
        if count is None:
            count = DEVICE_COUNT
        if factory_id is None:
            factory_id = DEFAULT_FACTORY_ID
        
        devices = []
        device_types = list(DEVICE_TYPES.keys())
        
        for i in range(count):
            # Generate device ID
            device_id = f"mock-device-{i+1:03d}"
            
            # Select device type (distribute evenly)
            device_type = device_types[i % len(device_types)]
            
            # Generate location
            location = f"{DEFAULT_LOCATION} - Device {i+1}"
            
            # Create device
            device = MockIoTDevice(
                device_id=device_id,
                device_type=device_type,
                factory_id=factory_id,
                location=location
            )
            
            devices.append(device)
            self.devices[device_id] = device
            
            logger.info(f"Created device: {device_id} ({device_type})")
        
        return devices
    
    def register_devices_with_api(self) -> bool:
        """Register all devices with the API Gateway"""
        logger.info("Registering devices with API Gateway...")
        
        success_count = 0
        for device_id, device in self.devices.items():
            try:
                device_info = device.get_info()
                
                # Prepare device data for API
                device_data = {
                    'name': f"Mock {device_info['deviceType'].replace('_', ' ').title()}",
                    'address': f"192.168.1.{100 + len(self.registered_devices)}",
                    'type': device_info['deviceType'],
                    'status': device_info['status'],
                    'factoryId': device_info['factoryId'],
                    'location': device_info['location'],
                    'configuration': {
                        'sampling_rate': str(device_info['samplingRate']),
                        'mock_device': 'true',
                        'created_by': 'mock_device_manager'
                    }
                }
                
                # Register device via API
                response = requests.post(
                    f"{API_BASE_URL}/devices",
                    json=device_data,
                    headers={'Content-Type': 'application/json'},
                    timeout=10
                )
                
                if response.status_code == 201:
                    logger.info(f"Successfully registered device: {device_id}")
                    self.registered_devices.append(device_id)
                    success_count += 1
                else:
                    logger.error(f"Failed to register device {device_id}: {response.status_code} - {response.text}")
                    
            except Exception as e:
                logger.error(f"Error registering device {device_id}: {e}")
        
        logger.info(f"Registered {success_count}/{len(self.devices)} devices successfully")
        return success_count > 0
    
    def start_all_devices(self):
        """Start all devices"""
        logger.info("Starting all devices...")
        
        for device_id, device in self.devices.items():
            try:
                # Connect to MQTT
                if device.connect():
                    # Start device in separate thread
                    thread = threading.Thread(
                        target=device.run,
                        name=f"device-{device_id}",
                        daemon=True
                    )
                    thread.start()
                    self.device_threads[device_id] = thread
                    logger.info(f"Started device: {device_id}")
                else:
                    logger.error(f"Failed to start device: {device_id}")
                    
            except Exception as e:
                logger.error(f"Error starting device {device_id}: {e}")
        
        self.is_running = True
        logger.info(f"Started {len(self.device_threads)} devices")
    
    def stop_all_devices(self):
        """Stop all devices"""
        logger.info("Stopping all devices...")
        
        self.is_running = False
        
        for device_id, device in self.devices.items():
            try:
                device.disconnect()
                logger.info(f"Stopped device: {device_id}")
            except Exception as e:
                logger.error(f"Error stopping device {device_id}: {e}")
        
        # Wait for threads to finish
        for device_id, thread in self.device_threads.items():
            try:
                thread.join(timeout=5)
                logger.info(f"Device thread {device_id} finished")
            except Exception as e:
                logger.error(f"Error waiting for device thread {device_id}: {e}")
        
        self.device_threads.clear()
        logger.info("All devices stopped")
    
    def get_device_status(self) -> Dict[str, any]:
        """Get status of all devices"""
        status = {
            'total_devices': len(self.devices),
            'running_devices': len(self.device_threads),
            'registered_devices': len(self.registered_devices),
            'devices': {}
        }
        
        for device_id, device in self.devices.items():
            status['devices'][device_id] = device.get_info()
        
        return status
    
    def send_broadcast_command(self, command: Dict[str, any]) -> bool:
        """Send broadcast command to all devices"""
        logger.info(f"Sending broadcast command: {command}")
        
        try:
            response = requests.post(
                f"{API_BASE_URL}/processor/devices/all/control",
                json=command,
                headers={'Content-Type': 'application/json'},
                timeout=10
            )
            
            if response.status_code == 200:
                logger.info("Broadcast command sent successfully")
                return True
            else:
                logger.error(f"Failed to send broadcast command: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            logger.error(f"Error sending broadcast command: {e}")
            return False
    
    def send_device_command(self, device_id: str, command: Dict[str, any]) -> bool:
        """Send command to specific device"""
        logger.info(f"Sending command to device {device_id}: {command}")
        
        try:
            response = requests.post(
                f"{API_BASE_URL}/processor/devices/{device_id}/control",
                json=command,
                headers={'Content-Type': 'application/json'},
                timeout=10
            )
            
            if response.status_code == 200:
                logger.info(f"Command sent to device {device_id} successfully")
                return True
            else:
                logger.error(f"Failed to send command to device {device_id}: {response.status_code} - {response.text}")
                return False
                
        except Exception as e:
            logger.error(f"Error sending command to device {device_id}: {e}")
            return False
    
    def get_device_reports(self) -> Dict[str, any]:
        """Get reports for all devices"""
        reports = {}
        
        for device_id in self.devices.keys():
            try:
                response = requests.get(
                    f"{API_BASE_URL}/analysis/devices/{device_id}/report?hours=1",
                    timeout=10
                )
                
                if response.status_code == 200:
                    reports[device_id] = response.json()
                else:
                    logger.error(f"Failed to get report for device {device_id}: {response.status_code}")
                    
            except Exception as e:
                logger.error(f"Error getting report for device {device_id}: {e}")
        
        return reports
    
    def simulate_anomaly(self, device_id: str) -> bool:
        """Simulate anomaly for specific device"""
        if device_id not in self.devices:
            logger.error(f"Device {device_id} not found")
            return False
        
        device = self.devices[device_id]
        
        # Force anomaly by modifying device state
        device.anomaly_count += 1
        
        # Send anomaly data
        anomaly_data = device.generate_sensor_data()
        if anomaly_data:
            # Force anomaly flag
            anomaly_data['data']['isAnomaly'] = True
            device.send_data(anomaly_data)
            logger.info(f"Simulated anomaly for device {device_id}")
            return True
        
        return False
    
    def simulate_device_failure(self, device_id: str) -> bool:
        """Simulate device failure"""
        if device_id not in self.devices:
            logger.error(f"Device {device_id} not found")
            return False
        
        device = self.devices[device_id]
        device.emergency_stop()
        logger.info(f"Simulated failure for device {device_id}")
        return True
    
    def restore_device(self, device_id: str) -> bool:
        """Restore device to normal operation"""
        if device_id not in self.devices:
            logger.error(f"Device {device_id} not found")
            return False
        
        device = self.devices[device_id]
        device.start()
        logger.info(f"Restored device {device_id}")
        return True
    
    def run_interactive_mode(self):
        """Run interactive mode for device management"""
        logger.info("Starting interactive mode...")
        
        while True:
            try:
                print("\n" + "="*50)
                print("Mock IoT Device Manager - Interactive Mode")
                print("="*50)
                print("1. Show device status")
                print("2. Send broadcast command")
                print("3. Send command to specific device")
                print("4. Simulate anomaly")
                print("5. Simulate device failure")
                print("6. Restore device")
                print("7. Get device reports")
                print("8. Exit")
                print("-"*50)
                
                choice = input("Enter your choice (1-8): ").strip()
                
                if choice == '1':
                    status = self.get_device_status()
                    print(f"\nDevice Status:")
                    print(f"Total devices: {status['total_devices']}")
                    print(f"Running devices: {status['running_devices']}")
                    print(f"Registered devices: {status['registered_devices']}")
                    
                    for device_id, info in status['devices'].items():
                        print(f"\n{device_id}:")
                        print(f"  Type: {info['deviceType']}")
                        print(f"  Status: {info['status']}")
                        print(f"  Battery: {info['batteryLevel']:.1f}%")
                        print(f"  Data Count: {info['dataCounter']}")
                        print(f"  Anomalies: {info['anomalyCount']}")
                
                elif choice == '2':
                    print("\nBroadcast Commands:")
                    print("1. START")
                    print("2. STOP")
                    print("3. RESTART")
                    print("4. EMERGENCY_STOP")
                    print("5. STATUS_CHECK")
                    
                    cmd_choice = input("Enter command choice (1-5): ").strip()
                    commands = ['START', 'STOP', 'RESTART', 'EMERGENCY_STOP', 'STATUS_CHECK']
                    
                    if cmd_choice.isdigit() and 1 <= int(cmd_choice) <= 5:
                        command = {
                            'commandId': f"broadcast-{int(time.time())}",
                            'commandType': commands[int(cmd_choice)-1],
                            'payload': f"{commands[int(cmd_choice)-1].lower()}_all_devices"
                        }
                        self.send_broadcast_command(command)
                    else:
                        print("Invalid choice")
                
                elif choice == '3':
                    device_id = input("Enter device ID: ").strip()
                    if device_id in self.devices:
                        print("\nDevice Commands:")
                        print("1. START")
                        print("2. STOP")
                        print("3. RESTART")
                        print("4. STATUS_CHECK")
                        
                        cmd_choice = input("Enter command choice (1-4): ").strip()
                        commands = ['START', 'STOP', 'RESTART', 'STATUS_CHECK']
                        
                        if cmd_choice.isdigit() and 1 <= int(cmd_choice) <= 4:
                            command = {
                                'commandId': f"device-{int(time.time())}",
                                'commandType': commands[int(cmd_choice)-1],
                                'payload': f"{commands[int(cmd_choice)-1].lower()}_device"
                            }
                            self.send_device_command(device_id, command)
                        else:
                            print("Invalid choice")
                    else:
                        print(f"Device {device_id} not found")
                
                elif choice == '4':
                    device_id = input("Enter device ID: ").strip()
                    self.simulate_anomaly(device_id)
                
                elif choice == '5':
                    device_id = input("Enter device ID: ").strip()
                    self.simulate_device_failure(device_id)
                
                elif choice == '6':
                    device_id = input("Enter device ID: ").strip()
                    self.restore_device(device_id)
                
                elif choice == '7':
                    print("Getting device reports...")
                    reports = self.get_device_reports()
                    for device_id, report in reports.items():
                        print(f"\n{device_id}: {len(report.get('data', []))} data points")
                
                elif choice == '8':
                    print("Exiting interactive mode...")
                    break
                
                else:
                    print("Invalid choice. Please try again.")
                    
            except KeyboardInterrupt:
                print("\nExiting interactive mode...")
                break
            except Exception as e:
                logger.error(f"Error in interactive mode: {e}")
                print(f"Error: {e}")
        
        self.stop_all_devices()
