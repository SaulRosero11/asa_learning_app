package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateModuleRequest(
        @NotBlank(message = "El título del módulo es requerido")
        String title
) {}
