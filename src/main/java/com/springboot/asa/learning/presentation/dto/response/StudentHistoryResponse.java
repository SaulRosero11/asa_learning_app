package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StudentHistoryResponse(
        UUID userId,
        String email,
        List<EnrollmentHistory> enrollments
) {
    public record EnrollmentHistory(
            UUID programId,
            String programName,
            String status,
            OffsetDateTime enrolledAt,
            OffsetDateTime completionDate,
            List<AttemptHistory> attempts
    ) {}

    public record AttemptHistory(
            UUID assessmentId,
            String assessmentTitle,
            BigDecimal totalScore,
            BigDecimal percentage,
            OffsetDateTime endTime
    ) {}
}
