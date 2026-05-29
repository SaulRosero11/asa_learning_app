package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserResultSummary(
        UUID userId,
        String userEmail,
        String assessmentType,
        int attemptCount,
        BigDecimal bestScore,
        OffsetDateTime lastAttemptAt,
        List<AttemptDetailResponse> attempts
) {}
