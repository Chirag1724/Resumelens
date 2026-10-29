package com.resumelens.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resumeLensOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("ResumeLens REST API")
                        .description("Deterministic ATS Resume Scanner & Scoring Engine REST API. " +
                                "Provides deterministic TF-IDF keyword extraction, Cosine Similarity matching, " +
                                "and 6-dimension mathematical evaluation.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Chirag Dwivedi")
                                .email("chiragdwivediji@gmail.com")
                                .url("https://github.com/Chirag1724/Resumelens"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://github.com/Chirag1724/Resumelens/blob/main/LICENSE")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token obtained from /api/v1/auth/login or /api/v1/auth/register")));
    }
}
