package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateNewsRequest(
        @NotBlank String title,
        @NotBlank String content
) {}
