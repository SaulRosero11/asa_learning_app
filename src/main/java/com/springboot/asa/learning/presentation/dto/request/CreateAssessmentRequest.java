package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CreateAssessmentRequest(
        @NotBlank String title,
        String type,
        UUID moduleId,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        @Min(1) int maxAttempts,
        boolean published,
        @Valid List<CreateQuestionRequest> questions
) {}
