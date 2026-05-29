package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateLessonRequest(
        @NotBlank(message = "El título de la lección es requerido")
        String title,

        String contentUrl
) {}
