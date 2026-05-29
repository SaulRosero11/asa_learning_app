package com.springboot.asa.learning.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record OnboardingQuestionResponse(
        UUID id,
        String label,
        String fieldKey,
        String type,
        List<String> options,
        int displayOrder,
        boolean required,
        boolean active
) {}
