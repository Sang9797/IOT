import paho.mqtt.client as mqtt
import json
import random
import time
from datetime import datetime
import ssl

class MQTTTestPublisher:
    def __init__(self):
        self.host = "localhost"
        self.port = 8883
        self.topic = "iot/sensors/iot_device_01"

        # Certificate paths
        self.ca_cert = "/home/sanglee/cursor/IOT/mosquitto/config-extend/cert/ca-bundle.crt"
        self.client_cert = "/home/sanglee/cursor/IOT/mosquitto/config-extend/cert/iot_device_01.crt"
        self.client_key = "/home/sanglee/cursor/IOT/mosquitto/config-extend/cert/iot_device_01.key"

        # Message types configuration
        self.message_types = {
            'TEMPERATURE_SENSOR': {
                'range': (0, 100),
                'unit': '°C',
                'key': 'temperature'
            },
            'PRESSURE_SENSOR': {
                'range': (0, 10000),
                'unit': 'bar',
                'key': 'pressure'
            }
        }

        self.client = None

    def setup_client(self):
        """Initialize and configure MQTT client with TLS"""
        self.client = mqtt.Client()

        # Configure TLS
        self.client.tls_set(
            ca_certs=self.ca_cert,
            certfile=self.client_cert,
            keyfile=self.client_key,
            cert_reqs=ssl.CERT_REQUIRED,
            tls_version=ssl.PROTOCOL_TLS
        )

        # Set callbacks
        self.client.on_connect = self.on_connect
        self.client.on_publish = self.on_publish

        return self.client

    def on_connect(self, client, userdata, flags, rc):
        """Callback for when client connects to broker"""
        if rc == 0:
            print(f"✓ Connected successfully to {self.host}:{self.port}")
        else:
            print(f"✗ Connection failed with code {rc}")

    def on_publish(self, client, userdata, mid):
        """Callback for when message is published"""
        print(f"✓ Message published (mid: {mid})")

    def generate_message_by_type(self, message_type, timestamp=None):
        """Generate message for a specific message type"""
        if timestamp is None:
            timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")

        type_config = self.message_types[message_type]
        data_key = type_config['key']
        sensor_value = round(random.uniform(*type_config['range']), 1)

        message = {
            "deviceId": "mock-device-001",
            "timestamp": timestamp,
            "factoryId": "factory-001",
            "location": "Production Line A - Device 1",
            "messageType": message_type,
            "batteryLevel": round(random.uniform(0, 100), 1),
            "signalStrength": random.randint(-90, -30),
            "data": {
                data_key: sensor_value,
                "unit": type_config['unit']
            }
        }

        return message

    def publish_message(self):
        """Generate and publish messages for all message types"""
        timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")

        print(f"\n{'='*60}")
        print(f"Publishing all message types at {timestamp}")
        print(f"{'='*60}")

        results = []
        for message_type in self.message_types.keys():
            try:
                type_config = self.message_types[message_type]
                data_key = type_config['key']

                message = self.generate_message_by_type(message_type, timestamp)
                message_json = json.dumps(message, indent=2)

                print(f"\n  📤 {message['messageType']}:")
                print(f"     {data_key.capitalize()}: {message['data'][data_key]} {message['data']['unit']}")
                print(f"     Battery: {message['batteryLevel']}% | Signal: {message['signalStrength']} dBm")

                result = self.client.publish(self.topic, message_json, qos=1)
                results.append(result)
                time.sleep(0.1)  # Small delay between messages
            except Exception as e:
                print(f"     ✗ Error publishing {message_type}: {e}")
                import traceback
                traceback.print_exc()

        print(f"\n{'='*60}")
        print(f"✓ Published {len(results)} messages")
        print(f"{'='*60}")

        return results

    def run_continuous(self, interval_seconds=60):
        """Run publisher continuously with specified interval"""
        try:
            # Setup and connect
            self.setup_client()
            self.client.connect(self.host, self.port, keepalive=60)
            self.client.loop_start()

            print(f"\n🚀 MQTT Test Publisher Started")
            print(f"Publishing to: {self.topic}")
            print(f"Interval: {interval_seconds} seconds")
            print(f"Press Ctrl+C to stop\n")

            while True:
                self.publish_message()
                time.sleep(interval_seconds)

        except KeyboardInterrupt:
            print("\n\n⏹ Stopping publisher...")
        except Exception as e:
            print(f"\n✗ Error: {e}")
        finally:
            if self.client:
                self.client.loop_stop()
                self.client.disconnect()
                print("✓ Disconnected from broker")

    def run_once(self):
        """Publish messages for all message types"""
        try:
            self.setup_client()
            self.client.connect(self.host, self.port, keepalive=60)
            self.client.loop_start()

            time.sleep(1)  # Wait for connection
            self.publish_message()
            time.sleep(2)  # Wait for publish to complete

        except Exception as e:
            print(f"✗ Error: {e}")
        finally:
            if self.client:
                self.client.loop_stop()
                self.client.disconnect()

if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='MQTT Test Message Publisher')
    parser.add_argument(
        '--interval',
        type=int,
        default=60,
        help='Interval between messages in seconds (default: 60)'
    )
    parser.add_argument(
        '--once',
        action='store_true',
        help='Publish only one message and exit'
    )

    args = parser.parse_args()

    publisher = MQTTTestPublisher()

    if args.once:
        publisher.run_once()
    else:
        publisher.run_continuous(args.interval)