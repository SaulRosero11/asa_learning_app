package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public record CreateProgramRequest(
        @NotBlank(message = "El nombre del programa es obligatorio")
        String name,

        String description,

        OffsetDateTime startDate,
        OffsetDateTime endDate,

        String imageUrl
) {}
