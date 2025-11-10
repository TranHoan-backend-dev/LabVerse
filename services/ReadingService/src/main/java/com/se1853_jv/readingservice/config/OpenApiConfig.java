package com.se1853_jv.readingservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI readingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reading Service API")
                        .description("API documentation for LabVerse Reading Workflow Service")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("LabVerse Team")
                                .email("support@labverse.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/reading-service")
                                .description("Gateway Server (Recommended)"),
                        new Server()
                                .url(contextPath.isEmpty() ? "/" : contextPath)
                                .description("Direct Service")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("LabVerse Documentation")
                        .url("https://labverse.com/docs"));
    }
}

