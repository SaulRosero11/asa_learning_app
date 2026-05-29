package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ExamSummaryResponse(
        UUID assessmentId,
        String title,
        String type,
        int attemptCount,
        BigDecimal bestScore
) {}
