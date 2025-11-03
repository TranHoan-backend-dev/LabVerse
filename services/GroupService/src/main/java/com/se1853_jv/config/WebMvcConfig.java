package com.se1853_jv.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Cho phép trailing slash trong URL paths
        configurer.setUseTrailingSlashMatch(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Disable default static resource handlers để tránh xung đột với API paths
        // Chỉ serve static resources từ /static và /public nếu cần
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}


