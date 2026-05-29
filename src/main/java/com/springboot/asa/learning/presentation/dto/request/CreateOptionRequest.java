package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOptionRequest(
        @NotBlank String optionText,
        boolean isCorrect
) {}
