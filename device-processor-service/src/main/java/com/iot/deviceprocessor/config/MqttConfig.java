package com.iot.deviceprocessor.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Slf4j
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker.url:ssl://localhost:8883}")
    private String mqttBrokerUrl;

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

    @Value("${mqtt.ssl.truststore.password:client1234}")
    private String truststorePassword;

    @Value("${mqtt.ssl.truststore.type:JKS}")
    private String truststoreType;

    @Value("${mqtt.ssl.keystore.path:classpath:certs/keystore.jks}")
    private Resource keystorePath;

    @Value("${mqtt.ssl.keystore.password:client1234}")
    private String keystorePassword;

    @Value("${mqtt.ssl.keystore.type:JKS}")
    private String keystoreType;

    @Value("${mqtt.ssl.protocol:TLSv1.2}")
    private String sslProtocol;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        log.info("Creating MQTT client...");
        log.info("Broker URL: {}", mqttBrokerUrl);
        log.info("Client ID: {}", mqttClientId);
        log.info("SSL Enabled: {}", sslEnabled);

        MqttClient client = new MqttClient(mqttBrokerUrl, mqttClientId);
        return client;
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        log.info("Configuring MQTT connection options...");

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);

        // Set username and password if provided
        if (mqttUsername != null && !mqttUsername.isEmpty()) {
            log.info("Setting MQTT username: {}", mqttUsername);
            options.setUserName(mqttUsername);
        }

        if (mqttPassword != null && !mqttPassword.isEmpty()) {
            log.info("Setting MQTT password: [PROTECTED]");
            options.setPassword(mqttPassword.toCharArray());
        }

        // Configure SSL/TLS if enabled
        if (sslEnabled) {
            try {
                log.info("Configuring SSL/TLS...");
                log.info("Protocol: {}", sslProtocol);
                log.info("Truststore: {}", truststorePath.getDescription());
                log.info("Keystore: {}", keystorePath.getDescription());

                SSLSocketFactory socketFactory = createSSLSocketFactory();
                options.setSocketFactory(socketFactory);

                log.info("✅ SSL/TLS configured successfully");
            } catch (Exception e) {
                log.error("❌ Failed to configure SSL/TLS", e);
                throw new RuntimeException("Failed to configure SSL/TLS for MQTT", e);
            }
        } else {
            log.warn("⚠️  SSL/TLS is disabled - connection will not be encrypted");
        }

        return options;
    }

    private SSLSocketFactory createSSLSocketFactory() throws Exception {
        log.debug("Creating SSL socket factory...");

        // Step 1: Load and initialize TrustStore (contains CA certificates)
        log.debug("Loading truststore...");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
        );
        KeyStore trustStore = KeyStore.getInstance(truststoreType);

        try (InputStream tsInput = truststorePath.getInputStream()) {
            trustStore.load(tsInput, truststorePassword.toCharArray());
            int certCount = trustStore.size();
            log.debug("Truststore loaded successfully with {} certificate(s)", certCount);

            // Log certificate aliases for debugging
            if (log.isDebugEnabled()) {
                trustStore.aliases().asIterator().forEachRemaining(alias ->
                        log.debug("  - Truststore contains: {}", alias)
                );
            }
        } catch (Exception e) {
            log.error("Failed to load truststore from: {}", truststorePath.getDescription(), e);
            throw new RuntimeException("Failed to load truststore", e);
        }

        tmf.init(trustStore);
        log.debug("TrustManager initialized successfully");

        // Step 2: Load and initialize KeyStore (contains client certificate and private key)
        KeyManagerFactory kmf = null;
        if (keystorePath != null && keystorePath.exists()) {
            log.debug("Loading keystore...");
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance(keystoreType);

            try (InputStream ksInput = keystorePath.getInputStream()) {
                keyStore.load(ksInput, keystorePassword.toCharArray());
                int keyCount = keyStore.size();
                log.debug("Keystore loaded successfully with {} key(s)", keyCount);

                // Log key aliases for debugging
                if (log.isDebugEnabled()) {
                    keyStore.aliases().asIterator().forEachRemaining(alias ->
                            log.debug("  - Keystore contains: {}", alias)
                    );
                }
            } catch (Exception e) {
                log.error("Failed to load keystore from: {}", keystorePath.getDescription(), e);
                throw new RuntimeException("Failed to load keystore", e);
            }

            kmf.init(keyStore, keystorePassword.toCharArray());
            log.debug("KeyManager initialized successfully");
        } else {
            log.warn("Keystore not found or not configured - client certificate authentication will not be used");
        }

        // Step 3: Create and initialize SSLContext
        log.debug("Initializing SSLContext with protocol: {}", sslProtocol);
        SSLContext sslContext = SSLContext.getInstance(sslProtocol);
        sslContext.init(
                kmf != null ? kmf.getKeyManagers() : null,
                tmf.getTrustManagers(),
                null
        );

        log.debug("SSLContext initialized successfully");
        return sslContext.getSocketFactory();
    }
}