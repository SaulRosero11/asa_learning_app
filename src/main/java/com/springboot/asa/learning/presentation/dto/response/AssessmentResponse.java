package com.springboot.asa.learning.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AssessmentResponse(
        UUID id,
        UUID programId,
        UUID moduleId,
        String title,
        String type,
        boolean published,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        int maxAttempts,
        int attemptsUsed,
        List<QuestionResponse> questions,
        OffsetDateTime createdAt
) {}
