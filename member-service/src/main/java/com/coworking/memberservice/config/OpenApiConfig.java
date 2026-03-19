package com.coworking.memberservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI memberServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Member Service API")
                        .version("1.0")
                        .description("API simple pour gerer les membres et quotas")
                        .contact(new Contact().name("Coworking Team")));
    }
}
