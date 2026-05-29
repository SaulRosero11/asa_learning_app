package com.springboot.asa.learning.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "asa.seed")
public class SeedProperties {
    private String adminEmail = "admin@solidaridadyaccion.org";
}
