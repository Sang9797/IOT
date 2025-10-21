package com.iot.deviceprocessor.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    
    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String mqttBrokerUrl;
    
    @Value("${mqtt.client.id:device-processor}")
    private String mqttClientId;
    
    @Value("${mqtt.username:}")
    private String mqttUsername;
    
    @Value("${mqtt.password:}")
    private String mqttPassword;
    
    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(mqttBrokerUrl, mqttClientId);
        return client;
    }
    
    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);
        
        if (!mqttUsername.isEmpty()) {
            options.setUserName(mqttUsername);
        }
        if (!mqttPassword.isEmpty()) {
            options.setPassword(mqttPassword.toCharArray());
        }
        
        return options;
    }
}
