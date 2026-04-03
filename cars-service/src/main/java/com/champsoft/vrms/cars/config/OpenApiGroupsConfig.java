package com.champsoft.vrms.cars.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupsConfig {

    @Bean
    GroupedOpenApi carsApi() {
        return GroupedOpenApi.builder()
                .group("cars")
                .pathsToMatch("/api/cars/**")
                .build();
    }
}