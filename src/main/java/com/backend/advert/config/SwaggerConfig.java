package com.backend.advert.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("KakaoPay Advertisement API")
                        .version("v1")
                        .description("API documentation for KakaoPay Advertisement service"));
    }

    @Bean
    public GroupedOpenApi kakaoPayApi() {
        return GroupedOpenApi.builder()
                .group("KakaoPay Advertisement API")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
