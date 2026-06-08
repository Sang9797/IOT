package com.iot.analysisreport;

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
public class AnalysisReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalysisReportApplication.class, args);
    }
}
