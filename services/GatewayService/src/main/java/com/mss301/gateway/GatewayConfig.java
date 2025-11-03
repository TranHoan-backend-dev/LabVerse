package com.mss301.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GatewayConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow all origins
        corsConfig.setAllowedOriginPatterns(List.of("*"));
        
        // Allow all HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Allow all headers
        corsConfig.setAllowedHeaders(List.of("*"));
        
        // Expose all headers
        corsConfig.setExposedHeaders(List.of("*"));
        
        // Allow credentials (set to false when using wildcard origin)
        corsConfig.setAllowCredentials(false);
        
        // Cache preflight response
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // 1️⃣ Book / BookShelf / Review Service
                .route("book_service", r -> r.path(
                                "/api/books/**",
                                "/api/bookshelves/**",
                                "/api/reviews/**")
                        .uri("http://localhost:8081"))

                // 2️⃣ Fine Service
                .route("fine_service", r -> r.path("/api/fines/**")
                        .uri("http://localhost:8086"))

                // 3️⃣ Loan Service
                .route("loan_service", r -> r.path("/api/loans/**")
                        .uri("http://localhost:8091"))

                // 4️⃣ User / Role Service
                .route("user_service", r -> r.path(
                                "/api/users/**",
                                "/api/roles/**")
                        .uri("http://localhost:8096"))

                // 5️⃣ Notification Service
                .route("notification_service", r -> r.path("/api/notifications/**")
                        .uri("http://localhost:8101"))

                .build();
    }
}
