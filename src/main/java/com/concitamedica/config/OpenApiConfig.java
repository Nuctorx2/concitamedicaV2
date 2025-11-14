package com.concitamedica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Definimos un nombre de referencia para nuestro esquema de seguridad
        final String securitySchemeName = "bearerAuth";

        // 1. Definimos el ESQUEMA de seguridad
        // Le decimos a Swagger: "Mi API usa un esquema de seguridad tipo HTTP,
        // que es un esquema 'bearer' (portador) y usa el formato 'JWT'".
        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // 2. Definimos el REQUISITO de seguridad
        // Le decimos a Swagger: "Para CUALQUIER endpoint,
        // se debe aplicar el esquema de seguridad 'bearerAuth'".
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securitySchemeName);

        // 3. Construimos y devolvemos el objeto OpenAPI
        return new OpenAPI()
                // Añadimos la información básica de la API (título, versión)
                .info(new Info().title("ConCitaMedica API")
                        .version("1.0.0")
                        .description("API para la gestión de citas médicas."))
                // Añadimos el requisito de seguridad GLOBALMENTE
                .addSecurityItem(securityRequirement)
                // Añadimos la DEFINICIÓN del esquema de seguridad
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, securityScheme)
                );
    }
}