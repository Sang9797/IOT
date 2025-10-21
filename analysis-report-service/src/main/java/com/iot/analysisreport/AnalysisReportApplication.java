package com.iot.analysisreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableEurekaClient
@EnableKafka
@EnableScheduling
public class AnalysisReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalysisReportApplication.class, args);
    }
}
