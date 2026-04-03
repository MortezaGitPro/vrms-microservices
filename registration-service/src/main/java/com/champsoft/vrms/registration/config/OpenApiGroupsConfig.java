package com.champsoft.vrms.registration.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupsConfig {

    @Bean
    GroupedOpenApi registrationApi() {
        return GroupedOpenApi.builder()
                .group("registration")
                .pathsToMatch("/api/registrations/**")
                .build();
    }
}