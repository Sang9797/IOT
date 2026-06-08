package com.iot.deviceprocessor.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;

public interface ManagedMqttClientFactory {

    ManagedMqttClient create(String brokerUrl, String clientId) throws MqttException;
}
