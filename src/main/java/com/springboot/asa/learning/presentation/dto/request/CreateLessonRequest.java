package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateLessonRequest(
        @NotBlank(message = "El título de la lección es requerido")
        String title,

        @NotBlank
        @Pattern(regexp = "VIDEO|TEXT|PDF", message = "contentType debe ser VIDEO, TEXT o PDF")
        String contentType,

        String contentUrl
) {}
