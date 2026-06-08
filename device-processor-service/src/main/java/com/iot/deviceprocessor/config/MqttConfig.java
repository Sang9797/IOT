package com.iot.deviceprocessor.config;

import com.iot.common.dto.MqttBrokerDto;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class MqttConfig {

    @Value("${mqtt.client.id:device-processor}")
    private String mqttClientId;

    @Value("${mqtt.username:}")
    private String mqttUsername;

    @Value("${mqtt.password:}")
    private String mqttPassword;

    @Value("${mqtt.ssl.enabled:true}")
    private boolean sslEnabled;

    @Value("${mqtt.ssl.truststore.path:classpath:certs/truststore.jks}")
    private Resource truststorePath;

    @Value("${mqtt.ssl.truststore.password:kafka1234}")
    private String truststorePassword;

    @Value("${mqtt.ssl.truststore.type:JKS}")
    private String truststoreType;

    @Value("${mqtt.ssl.keystore.path:classpath:certs/keystore.jks}")
    private Resource keystorePath;

    @Value("${mqtt.ssl.keystore.password:kafka1234}")
    private String keystorePassword;

    @Value("${mqtt.ssl.keystore.type:JKS}")
    private String keystoreType;

    @Value("${mqtt.ssl.protocol:TLSv1.2}")
    private String sslProtocol;

    public String composeBrokerUrl(MqttBrokerDto broker) {
        return broker.getProtocol() + "://" + broker.getHost() + ":" + broker.getPort();
    }

    public String composeClientId(String brokerId) {
        return mqttClientId + "-" + brokerId;
    }

    public MqttConnectOptions createConnectOptions(MqttBrokerDto broker) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);

        String username = broker.getUsername() != null && !broker.getUsername().isBlank() ? broker.getUsername() : mqttUsername;
        String password = broker.getPassword() != null && !broker.getPassword().isBlank() ? broker.getPassword() : mqttPassword;
        if (username != null && !username.isBlank()) {
            options.setUserName(username);
        }
        if (password != null && !password.isBlank()) {
            options.setPassword(password.toCharArray());
        }

        boolean brokerUsesSsl = "ssl".equalsIgnoreCase(broker.getProtocol()) || "tls".equalsIgnoreCase(broker.getProtocol());
        if (sslEnabled && brokerUsesSsl) {
            try {
                options.setSocketFactory(createSslSocketFactory());
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to configure SSL/TLS for MQTT broker " + broker.getId(), exception);
            }
        }

        return options;
    }

    private SSLSocketFactory createSslSocketFactory() throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore trustStore = KeyStore.getInstance(truststoreType);
        try (InputStream tsInput = truststorePath.getInputStream()) {
            trustStore.load(tsInput, truststorePassword.toCharArray());
        }
        trustManagerFactory.init(trustStore);

        KeyManagerFactory keyManagerFactory = null;
        if (keystorePath != null && keystorePath.exists()) {
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            try (InputStream ksInput = keystorePath.getInputStream()) {
                keyStore.load(ksInput, keystorePassword.toCharArray());
            }
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
        }

        SSLContext sslContext = SSLContext.getInstance(sslProtocol);
        sslContext.init(
                keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                trustManagerFactory.getTrustManagers(),
                null
        );
        return sslContext.getSocketFactory();
    }
}
