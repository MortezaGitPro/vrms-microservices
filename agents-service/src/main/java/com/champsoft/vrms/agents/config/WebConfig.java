package com.champsoft.vrms.agents.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "OPTIONS");

        registry.addMapping("/swagger-ui/**")
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "OPTIONS");

        registry.addMapping("/swagger-ui.html")
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "OPTIONS");
    }
}