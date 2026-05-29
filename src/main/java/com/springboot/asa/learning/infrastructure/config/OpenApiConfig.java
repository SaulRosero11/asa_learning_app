package com.springboot.asa.learning.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI asaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ASA E-Learning API")
                        .description("API REST de la plataforma de capacitación de Solidaridad y Acción")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Solidaridad y Acción")
                                .email("sistemas@solidaridadyaccion.org")))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("asa_access_token")));
    }
}
