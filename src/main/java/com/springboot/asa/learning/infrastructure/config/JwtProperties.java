package com.springboot.asa.learning.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "asa.jwt")
public class JwtProperties {
    private String secret = "cambia-este-secreto-en-produccion-minimo-32-caracteres";
    private long accessExpirationMs = 900_000L;          // 15 minutos
    private long refreshExpirationShortMs = 1_800_000L;  // 30 minutos
    private long refreshExpirationLongMs = 2_592_000_000L; // 30 días
}
