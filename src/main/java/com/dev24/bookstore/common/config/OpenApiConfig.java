package com.dev24.bookstore.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookstoreOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("DEV24 Bookstore API")
                        .description("DEV24 도서 쇼핑몰 현대화 프로젝트 REST API 문서")
                        .version("v1"));
    }
}
