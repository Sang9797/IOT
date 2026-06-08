package com.iot.deviceprocessor;

import com.iot.common.config.CommonKafkaConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@Import(CommonKafkaConfiguration.class)
public class DeviceProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceProcessorApplication.class, args);
    }
}
