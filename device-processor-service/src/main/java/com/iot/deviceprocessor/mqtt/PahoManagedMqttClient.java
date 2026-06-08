package com.iot.deviceprocessor.mqtt;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PahoManagedMqttClient implements ManagedMqttClient {

    private final MqttClient mqttClient;

    public PahoManagedMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    @Override
    public void setCallback(MqttCallback callback) throws MqttException {
        mqttClient.setCallback(callback);
    }

    @Override
    public void connect(MqttConnectOptions options) throws MqttException {
        mqttClient.connect(options);
    }

    @Override
    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    @Override
    public void subscribe(String topicFilter, int qos) throws MqttException {
        mqttClient.subscribe(topicFilter, qos);
    }

    @Override
    public void publish(String topic, MqttMessage message) throws MqttException {
        mqttClient.publish(topic, message);
    }

    @Override
    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }

    @Override
    public void close() throws MqttException {
        mqttClient.close();
    }
}
