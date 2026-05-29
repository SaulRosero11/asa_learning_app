package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record OnboardingQuestionRequest(
        @NotBlank String label,
        String fieldKey,
        String type,
        List<String> options,
        Integer displayOrder,
        Boolean required,
        Boolean active
) {}
