"""
Demo script for Mock IoT Devices
Shows various features and capabilities
"""

import time
import json
import logging
from device_manager import DeviceManager

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


def demo_basic_functionality():
    """Demonstrate basic device functionality"""
    print("🎯 Demo 1: Basic Device Functionality")
    print("=" * 50)
    
    # Create device manager
    manager = DeviceManager()
    
    # Create a few devices
    devices = manager.create_devices(count=3, factory_id='demo-factory')
    print(f"Created {len(devices)} devices")
    
    # Start devices
    manager.start_all_devices()
    print("Started all devices")
    
    # Let devices run for a bit
    print("Letting devices run for 10 seconds...")
    time.sleep(10)
    
    # Show status
    status = manager.get_device_status()
    print(f"\nDevice Status:")
    print(f"Total devices: {status['total_devices']}")
    print(f"Running devices: {status['running_devices']}")
    
    for device_id, info in status['devices'].items():
        print(f"\n{device_id}:")
        print(f"  Type: {info['deviceType']}")
        print(f"  Status: {info['status']}")
        print(f"  Battery: {info['batteryLevel']:.1f}%")
        print(f"  Data Count: {info['dataCounter']}")
        print(f"  Anomalies: {info['anomalyCount']}")
    
    # Stop devices
    manager.stop_all_devices()
    print("\nStopped all devices")


def demo_control_commands():
    """Demonstrate control command functionality"""
    print("\n🎯 Demo 2: Control Commands")
    print("=" * 50)
    
    manager = DeviceManager()
    devices = manager.create_devices(count=2, factory_id='demo-factory')
    manager.start_all_devices()
    
    # Wait for devices to start
    time.sleep(2)
    
    # Get first device ID
    device_id = list(devices.keys())[0]
    
    # Test different commands
    commands = [
        {
            'commandId': 'demo-start-001',
            'commandType': 'START',
            'payload': 'start_operation'
        },
        {
            'commandId': 'demo-status-001',
            'commandType': 'STATUS_CHECK',
            'payload': 'check_status'
        },
        {
            'commandId': 'demo-stop-001',
            'commandType': 'STOP',
            'payload': 'stop_operation'
        }
    ]
    
    for command in commands:
        print(f"\nSending command: {command['commandType']}")
        success = manager.send_device_command(device_id, command)
        if success:
            print(f"✅ Command {command['commandType']} sent successfully")
        else:
            print(f"❌ Command {command['commandType']} failed")
        
        time.sleep(2)
    
    # Test broadcast command
    print(f"\nSending broadcast command...")
    broadcast_command = {
        'commandId': 'demo-broadcast-001',
        'commandType': 'STATUS_CHECK',
        'payload': 'check_all_status'
    }
    success = manager.send_broadcast_command(broadcast_command)
    if success:
        print("✅ Broadcast command sent successfully")
    else:
        print("❌ Broadcast command failed")
    
    manager.stop_all_devices()


def demo_anomaly_simulation():
    """Demonstrate anomaly simulation"""
    print("\n🎯 Demo 3: Anomaly Simulation")
    print("=" * 50)
    
    manager = DeviceManager()
    devices = manager.create_devices(count=2, factory_id='demo-factory')
    manager.start_all_devices()
    
    # Wait for devices to start
    time.sleep(2)
    
    # Get device IDs
    device_ids = list(devices.keys())
    
    # Simulate anomaly for first device
    device_id = device_ids[0]
    print(f"Simulating anomaly for device: {device_id}")
    success = manager.simulate_anomaly(device_id)
    if success:
        print("✅ Anomaly simulated successfully")
    else:
        print("❌ Anomaly simulation failed")
    
    time.sleep(3)
    
    # Simulate device failure for second device
    device_id = device_ids[1]
    print(f"Simulating device failure for device: {device_id}")
    success = manager.simulate_device_failure(device_id)
    if success:
        print("✅ Device failure simulated successfully")
    else:
        print("❌ Device failure simulation failed")
    
    time.sleep(3)
    
    # Restore the failed device
    print(f"Restoring device: {device_id}")
    success = manager.restore_device(device_id)
    if success:
        print("✅ Device restored successfully")
    else:
        print("❌ Device restoration failed")
    
    manager.stop_all_devices()


def demo_different_device_types():
    """Demonstrate different device types"""
    print("\n🎯 Demo 4: Different Device Types")
    print("=" * 50)
    
    from device import MockIoTDevice
    
    device_types = ['TEMPERATURE_SENSOR', 'PRESSURE_SENSOR', 'VIBRATION_MONITOR', 'CONTROL_VALVE']
    
    devices = []
    for i, device_type in enumerate(device_types):
        device = MockIoTDevice(
            device_id=f'demo-{device_type.lower()}-{i+1:03d}',
            device_type=device_type,
            factory_id='demo-factory',
            location=f'Demo Location {i+1}'
        )
        devices.append(device)
        print(f"Created {device_type}: {device.device_id}")
    
    # Start all devices
    for device in devices:
        device.connect()
        device.start()
    
    print("\nLetting devices run for 15 seconds...")
    time.sleep(15)
    
    # Show data from each device type
    print("\nDevice Data Summary:")
    for device in devices:
        info = device.get_info()
        print(f"\n{device.device_id} ({device.device_type}):")
        print(f"  Status: {info['status']}")
        print(f"  Battery: {info['batteryLevel']:.1f}%")
        print(f"  Data Count: {info['dataCounter']}")
        print(f"  Anomalies: {info['anomalyCount']}")
    
    # Stop all devices
    for device in devices:
        device.disconnect()
    
    print("\nStopped all devices")


def demo_data_generation():
    """Demonstrate data generation capabilities"""
    print("\n🎯 Demo 5: Data Generation")
    print("=" * 50)
    
    from device import MockIoTDevice
    
    # Create a temperature sensor
    device = MockIoTDevice(
        device_id='demo-temp-sensor-001',
        device_type='TEMPERATURE_SENSOR',
        factory_id='demo-factory',
        location='Demo Temperature Station'
    )
    
    device.start()
    
    print("Generating sample data...")
    print("Device Type | Value | Unit | Battery | Anomaly")
    print("-" * 50)
    
    for i in range(10):
        data = device.generate_sensor_data()
        if data:
            value = data['data']['value']
            unit = data['data']['unit']
            battery = data['batteryLevel']
            is_anomaly = data['data']['isAnomaly']
            
            anomaly_indicator = "⚠️" if is_anomaly else "✅"
            print(f"{device.device_type:15} | {value:6.1f} | {unit:4} | {battery:6.1f}% | {anomaly_indicator}")
        
        time.sleep(1)
    
    print(f"\nTotal anomalies generated: {device.anomaly_count}")


def main():
    """Run all demos"""
    print("🚀 Mock IoT Devices Demo")
    print("=" * 60)
    print("This demo showcases various features of the Mock IoT Devices simulator.")
    print("Make sure the IoT system is running before starting this demo.")
    print("=" * 60)
    
    try:
        # Run demos
        demo_basic_functionality()
        demo_control_commands()
        demo_anomaly_simulation()
        demo_different_device_types()
        demo_data_generation()
        
        print("\n🎉 All demos completed successfully!")
        print("\n📝 Demo Summary:")
        print("  ✅ Basic device functionality")
        print("  ✅ Control command handling")
        print("  ✅ Anomaly simulation")
        print("  ✅ Different device types")
        print("  ✅ Data generation")
        
        print("\n🚀 Next Steps:")
        print("  1. Run: python main.py --interactive")
        print("  2. Or: ./scripts/start_mock_devices.sh")
        print("  3. Check the IoT system dashboard for real-time data")
        
    except KeyboardInterrupt:
        print("\n\n⏹️ Demo interrupted by user")
    except Exception as e:
        print(f"\n❌ Demo error: {e}")
        logger.exception("Demo error details")


if __name__ == '__main__':
    main()
