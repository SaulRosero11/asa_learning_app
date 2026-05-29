package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReorderModuleRequest(
        @NotNull(message = "El nuevo índice de orden es requerido")
        Integer orderIndex
) {}
