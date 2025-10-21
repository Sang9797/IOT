package com.iot.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Device Management Service Routes
                .route("device-management", r -> r.path("/api/devices/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service", "device-management"))
                        .uri("lb://device-management-service"))
                
                // Device Processor Service Routes
                .route("device-processor", r -> r.path("/api/processor/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service", "device-processor"))
                        .uri("lb://device-processor-service"))
                
                // Analysis & Report Service Routes
                .route("analysis-report", r -> r.path("/api/analysis/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service", "analysis-report"))
                        .uri("lb://analysis-report-service"))
                
                // Notification Service Routes
                .route("notification", r -> r.path("/api/notifications/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service", "notification"))
                        .uri("lb://notification-service"))
                
                // WebSocket Routes for Real-time Updates
                .route("websocket", r -> r.path("/ws/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://notification-service"))
                
                .build();
    }
}
