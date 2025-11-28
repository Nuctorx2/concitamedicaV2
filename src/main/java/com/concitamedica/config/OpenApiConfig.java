package com.concitamedica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securitySchemeName);
        return new OpenAPI()
                .info(new Info().title("ConCitaMedica API")
                        .version("1.0.0")
                        .description("API para la gestión de citas médicas."))
                .servers(List.of(
                        new Server().url("https://localhost:8080").description("Servidor Local (HTTPS)")
                ))
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, securityScheme)
                );
    }
}