package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AttemptDetailResponse(
        UUID attemptId,
        UUID userId,
        String userEmail,
        BigDecimal totalScore,
        OffsetDateTime submittedAt,
        List<AnswerDetail> answers
) {
    public record AnswerDetail(
            UUID responseId,
            UUID questionId,
            String questionText,
            String questionType,
            String selectedOptionText,
            boolean correct,
            BigDecimal pointsAwarded,
            BigDecimal maxPoints
    ) {}
}
