package com.mss301.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        // CORS is handled by CorsWebFilter in GatewayConfig
        // Disable Spring Security CORS completely to let CorsWebFilter handle it
        // Permit all requests including OPTIONS
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.disable()) // Disable Spring Security CORS, use CorsWebFilter instead
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .build();
    }
}
