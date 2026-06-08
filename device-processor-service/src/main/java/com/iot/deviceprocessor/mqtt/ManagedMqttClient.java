package com.iot.deviceprocessor.mqtt;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface ManagedMqttClient {

    void setCallback(MqttCallback callback) throws MqttException;

    void connect(MqttConnectOptions options) throws MqttException;

    boolean isConnected();

    void subscribe(String topicFilter, int qos) throws MqttException;

    void publish(String topic, MqttMessage message) throws MqttException;

    void disconnect() throws MqttException;

    void close() throws MqttException;
}
