package com.concitamedica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS para permitir la comunicación
 * entre el frontend (Vue.js) y el backend (Spring Boot).
 *
 * Esta configuración es válida para entornos de desarrollo.
 * En producción se recomienda restringir los orígenes permitidos.
 */
@Configuration
public class CorsConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Aplica a todos los endpoints
                        .allowedOrigins("http://localhost:5173") // URL del frontend en desarrollo
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                        .allowedHeaders("*") // Permitir cualquier cabecera
                        .allowCredentials(true); // Permitir envío de credenciales (cookies, JWT)
            }
        };
    }
}
