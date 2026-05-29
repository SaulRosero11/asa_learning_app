package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AssessmentAttemptSummary(
        UUID attemptId,
        UUID userId,
        String userEmail,
        int attemptNumber,
        BigDecimal totalScore,
        BigDecimal maxScore,
        BigDecimal percentage,
        String status,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {}
