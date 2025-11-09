package com.mss301.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class GatewayConfig {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // Disabled - using Spring Cloud Gateway built-in CORS from application.properties
    // @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Use allowedOriginPatterns to support wildcards (works with allowCredentials)
        corsConfig.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://192.168.*.*:3000",
            "http://10.*.*.*:3000",
            "http://172.1[6-9].*.*:3000",
            "http://172.2[0-9].*.*:3000",
            "http://172.3[0-1].*.*:3000"
        ));
        
        // Allow all HTTP methods including OPTIONS for preflight
        corsConfig.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // Allow all headers - important for preflight
        corsConfig.setAllowedHeaders(Collections.singletonList("*"));
        
        // Expose all headers
        corsConfig.setExposedHeaders(Collections.singletonList("*"));
        
        // Allow credentials
        corsConfig.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        System.out.println("========================================");
        System.out.println("CorsWebFilter initialized!");
        System.out.println("Allowed origin patterns: " + corsConfig.getAllowedOriginPatterns());
        System.out.println("========================================");
        
        return new CorsWebFilter(source);
    }
}