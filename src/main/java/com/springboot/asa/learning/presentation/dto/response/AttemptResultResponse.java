package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AttemptResultResponse(
        UUID attemptId,
        BigDecimal totalScore,
        BigDecimal maxScore,
        BigDecimal percentage,
        String status,
        OffsetDateTime endTime,
        // null for SURVEY or when review is not allowed
        List<QuestionResultResponse> questionResults
) {}
