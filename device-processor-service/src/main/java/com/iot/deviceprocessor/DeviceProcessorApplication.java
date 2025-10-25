package com.iot.deviceprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class DeviceProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceProcessorApplication.class, args);
    }
}
