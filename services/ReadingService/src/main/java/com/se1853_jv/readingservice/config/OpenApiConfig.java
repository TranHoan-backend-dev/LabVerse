package com.se1853_jv.readingservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

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
                .externalDocs(new ExternalDocumentation()
                        .description("LabVerse Documentation")
                        .url("https://labverse.com/docs"));
    }
}

