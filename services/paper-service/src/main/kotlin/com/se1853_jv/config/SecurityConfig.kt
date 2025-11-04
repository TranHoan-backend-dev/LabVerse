package com.se1853_jv.config

import com.se1853_jv.model.enumerate.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
//import org.springframework.security.config.Customizer
//import org.springframework.security.config.annotation.web.builders.HttpSecurity
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
//import org.springframework.security.config.annotation.web.configurers.*
//import org.springframework.security.config.http.SessionCreationPolicy
//import org.springframework.security.web.SecurityFilterChain

private const val PREFIX: String = "/v1/api"

//@Configuration
//@EnableWebSecurity
class SecurityConfig {
//
//    @Bean
//    fun filterChain(http: HttpSecurity): SecurityFilterChain {
//        return http
//            .csrf { it.disable() }
//            .authorizeHttpRequests({ auth ->
//                auth
//                    .requestMatchers(
//                        PREFIX.plus("/citations/health"),
//                        PREFIX.plus("/papers/health"),
//                        PREFIX.plus("/tags/health")
//                    ).permitAll()
//                    .requestMatchers(
//                        PREFIX.plus("/tags/**"),
//                        PREFIX.plus("/papers/**"),
//                        PREFIX.plus("/citations/**")
//                    ).hasAnyRole(
//                        Role.STUDENT.toString(), Role.INTERN.toString(),
//                        Role.PI.toString(), Role.LAB_HEAD.toString(),
//                        Role.RESEARCHER.toString()
//                    )
//                    .anyRequest().authenticated()
//            })
//            .sessionManagement { session: SessionManagementConfigurer<HttpSecurity?> ->
//                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            }
//            .httpBasic(Customizer.withDefaults())
//            .build()
//    }
}