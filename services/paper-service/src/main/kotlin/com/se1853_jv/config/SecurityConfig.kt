package com.se1853_jv.config

import com.se1853_jv.model.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
//import org.springframework.security.config.Customizer
//import org.springframework.security.config.annotation.web.builders.HttpSecurity
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
//import org.springframework.security.config.annotation.web.configurers.*
//import org.springframework.security.config.http.SessionCreationPolicy
//import org.springframework.security.web.SecurityFilterChain
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

//@Configuration
//@EnableWebSecurity
class SecurityConfig {

//    @Bean
//    fun filterChainForCitation(http: HttpSecurity): SecurityFilterChain {
//        http
//            .csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
//            .sessionManagement { session: SessionManagementConfigurer<HttpSecurity?> ->
//                session.sessionCreationPolicy(
//                    SessionCreationPolicy.STATELESS
//                )
//            }
//            .authorizeHttpRequests({ auth ->
//                auth
//                    .requestMatchers("/api/auth/**").permitAll()
//                    .requestMatchers("/actuator/**").permitAll()
//                    .anyRequest().authenticated()
//            }
//            )
//        return http.build()
//        return http
//            .csrf { it.disable() }
//            .authorizeHttpRequests({ auth ->
//                auth
//                    .requestMatchers("/v1/api/papers/citation").hasAnyRole(
//                        Role.STUDENT.toString(), Role.INTERN.toString(),
//                        Role.PI.toString(), Role.LAB_HEADER.toString(),
//                        Role.RESEARCHER.toString(),
//                    )
//            })
//            .httpBasic(Customizer.withDefaults())
//            .build()
//    }
}