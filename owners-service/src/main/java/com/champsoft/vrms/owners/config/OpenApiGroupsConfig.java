package com.champsoft.vrms.owners.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupsConfig {

    @Bean
    GroupedOpenApi ownersApi() {
        return GroupedOpenApi.builder()
                .group("owners")
                .pathsToMatch("/api/owners/**")
                .build();
    }
}