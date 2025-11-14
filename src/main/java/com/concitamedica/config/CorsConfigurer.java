package com.concitamedica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // <-- 1. Sigue siendo una clase de configuración
public class CorsConfigurer implements WebMvcConfigurer { // <-- 2. PERO AHORA, implementa WebMvcConfigurer

    @Override // <-- 3. Y simplemente sobrescribe el método
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a todas las rutas de tu API
                .allowedOrigins("http://localhost:5173") // La URL de tu app Vue.js
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*") // Permite todas las cabeceras (incluyendo "Authorization")
                .allowCredentials(true); // ¡Crucial para que funcionen las cookies/tokens!
    }
}