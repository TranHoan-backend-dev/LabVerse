package com.se1853_jv.readingservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration for Reading Service
 * 
 * Note: CORS is primarily handled by the API Gateway (GatewayService).
 * This configuration is kept minimal to avoid duplicate CORS headers.
 * If accessing the service directly (not through Gateway), you may need to enable CORS here.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Disabled to avoid duplicate CORS headers when requests go through Gateway
    // Gateway already handles CORS configuration
    // Uncomment below if you need to access this service directly (bypassing Gateway)
    
    /*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:3000", "http://127.0.0.1:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    */
}

