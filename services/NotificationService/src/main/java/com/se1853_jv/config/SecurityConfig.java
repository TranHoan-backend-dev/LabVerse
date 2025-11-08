package com.se1853_jv.config;

import com.se1853_jv.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Vô hiệu hóa CSRF (vì là API)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Cấu hình phân quyền
                .authorizeHttpRequests(authz -> authz
                        // Cho phép truy cập các đường dẫn public TRƯỚC (quan trọng: thứ tự)
                        .requestMatchers(
                                "/v1/api/notifications/events", // Endpoint để các service khác gửi notification events
                                "/v1/api/notifications/queue", // Endpoint để xem queue status (test/debug)
                                "/v1/api/notifications/queue/**", // Cho phép cả query params và sub-paths
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()

                        // Tất cả các request /api/v1/... và /v1/api/... khác đều phải xác thực
                        .requestMatchers("/api/v1/**", "/v1/api/**").authenticated()

                        // Bất kỳ request nào khác (ngoài 2 mục trên) thì cho qua
                        .anyRequest().permitAll()
                )

                // 3. Cấu hình session, API là STATELESS (không lưu session)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Thêm bộ lọc JWT của chúng ta vào trước bộ lọc UsernamePassword
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}