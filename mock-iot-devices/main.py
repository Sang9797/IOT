"""
Main entry point for Mock IoT Devices
"""

import sys
import time
import logging
import argparse
import signal
from typing import Optional

from config import DEVICE_COUNT, DEFAULT_FACTORY_ID, LOG_LEVEL, LOG_FORMAT
from device_manager import DeviceManager

# Configure logging
logging.basicConfig(
    level=getattr(logging, LOG_LEVEL.upper()),
    format=LOG_FORMAT,
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('mock_devices.log')
    ]
)

logger = logging.getLogger(__name__)


class MockIoTDeviceSimulator:
    """Main simulator class"""
    
    def __init__(self):
        self.device_manager = DeviceManager()
        self.is_running = False
        
        # Setup signal handlers
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)
    
    def _signal_handler(self, signum, frame):
        """Handle shutdown signals"""
        logger.info(f"Received signal {signum}, shutting down...")
        self.stop()
        sys.exit(0)
    
    def start(self, device_count: int = None, factory_id: str = None, 
              register_devices: bool = True, interactive: bool = False):
        """Start the mock device simulator"""
        logger.info("Starting Mock IoT Device Simulator...")
        
        try:
            # Create devices
            devices = self.device_manager.create_devices(
                count=device_count or DEVICE_COUNT,
                factory_id=factory_id or DEFAULT_FACTORY_ID
            )
            
            logger.info(f"Created {len(devices)} mock devices")
            
            # Register devices with API if requested
            if register_devices:
                if not self.device_manager.register_devices_with_api():
                    logger.warning("Some devices failed to register with API")
            
            # Start all devices
            self.device_manager.start_all_devices()
            self.is_running = True
            
            logger.info("Mock IoT Device Simulator started successfully")
            
            if interactive:
                # Run interactive mode
                self.device_manager.run_interactive_mode()
            else:
                # Run in background mode
                self._run_background_mode()
                
        except Exception as e:
            logger.error(f"Error starting simulator: {e}")
            self.stop()
            raise
    
    def _run_background_mode(self):
        """Run in background mode"""
        logger.info("Running in background mode...")
        
        try:
            while self.is_running:
                # Print status every 60 seconds
                time.sleep(60)
                status = self.device_manager.get_device_status()
                logger.info(f"Status: {status['running_devices']}/{status['total_devices']} devices running")
                
        except KeyboardInterrupt:
            logger.info("Received keyboard interrupt")
        except Exception as e:
            logger.error(f"Error in background mode: {e}")
        finally:
            self.stop()
    
    def stop(self):
        """Stop the simulator"""
        if self.is_running:
            logger.info("Stopping Mock IoT Device Simulator...")
            self.device_manager.stop_all_devices()
            self.is_running = False
            logger.info("Mock IoT Device Simulator stopped")
    
    def get_status(self):
        """Get simulator status"""
        return {
            'is_running': self.is_running,
            'device_status': self.device_manager.get_device_status()
        }


def main():
    """Main function"""
    parser = argparse.ArgumentParser(description='Mock IoT Device Simulator')
    parser.add_argument('--count', type=int, default=DEVICE_COUNT,
                       help='Number of devices to create (default: 10)')
    parser.add_argument('--factory-id', type=str, default=DEFAULT_FACTORY_ID,
                       help='Factory ID for devices (default: factory-001)')
    parser.add_argument('--no-register', action='store_true',
                       help='Skip device registration with API')
    parser.add_argument('--interactive', action='store_true',
                       help='Run in interactive mode')
    parser.add_argument('--log-level', type=str, default=LOG_LEVEL,
                       choices=['DEBUG', 'INFO', 'WARNING', 'ERROR'],
                       help='Log level (default: INFO)')
    
    args = parser.parse_args()
    
    # Update log level
    logging.getLogger().setLevel(getattr(logging, args.log_level.upper()))
    
    # Create and start simulator
    simulator = MockIoTDeviceSimulator()
    
    try:
        simulator.start(
            device_count=args.count,
            factory_id=args.factory_id,
            register_devices=not args.no_register,
            interactive=args.interactive
        )
    except KeyboardInterrupt:
        logger.info("Received keyboard interrupt")
    except Exception as e:
        logger.error(f"Simulator error: {e}")
        sys.exit(1)
    finally:
        simulator.stop()


if __name__ == '__main__':
    main()
