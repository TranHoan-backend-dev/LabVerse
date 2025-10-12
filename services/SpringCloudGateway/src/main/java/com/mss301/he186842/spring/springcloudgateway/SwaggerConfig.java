package com.mss301.he186842.spring.springcloudgateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account_route", r -> r.path("/api/accounts/**")
                        .filters(f -> f.addRequestHeader("X-Service1", "Service1HeaderValue"))
                        .uri("http://localhost:8081"))
                .route("orchid_route", r -> r.path("/api/orchids/**")
                        .filters(f -> f.addRequestParameter("param", "value"))
                        .uri("http://localhost:8082"))
                .route("order_route", r -> r.path("/api/orders/**")
//                        .filters(f -> f.rewritePath("/orders/(?<segment>.*)", "/${segment}"))
                        .uri("http://localhost:8083"))
                .build();
    }
}
