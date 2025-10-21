package com.iot.analysisreport.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDbConfig {
    
    @Value("${influxdb.url:http://localhost:8086}")
    private String influxDbUrl;
    
    @Value("${influxdb.token:}")
    private String influxDbToken;
    
    @Value("${influxdb.org:iot-org}")
    private String influxDbOrg;
    
    @Value("${influxdb.bucket:iot-data}")
    private String influxDbBucket;
    
    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(influxDbUrl, influxDbToken.toCharArray(), influxDbOrg, influxDbBucket);
    }
    
    public String getBucket() {
        return influxDbBucket;
    }
    
    public String getOrg() {
        return influxDbOrg;
    }
}
