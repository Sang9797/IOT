package com.iot.deviceprocessor.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

@Component
public class PahoManagedMqttClientFactory implements ManagedMqttClientFactory {

    @Override
    public ManagedMqttClient create(String brokerUrl, String clientId) throws MqttException {
        return new PahoManagedMqttClient(new MqttClient(brokerUrl, clientId));
    }
}
