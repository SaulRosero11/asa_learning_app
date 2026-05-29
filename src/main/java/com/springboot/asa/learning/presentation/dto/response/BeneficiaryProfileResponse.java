package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record BeneficiaryProfileResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        boolean onboardingCompleted,
        int enrollmentCount,
        Map<String, Object> onboardingData,
        List<EnrollmentSummary> enrollments,
        List<LeaderProgramSummary> leaderPrograms,
        List<AttemptSummary> assessmentAttempts
) {
    public record EnrollmentSummary(
            UUID programId,
            String programName,
            String status,
            OffsetDateTime enrolledAt,
            OffsetDateTime completionDate
    ) {}

    public record LeaderProgramSummary(
            UUID programId,
            String programName
    ) {}

    public record AttemptSummary(
            UUID assessmentId,
            String assessmentTitle,
            String programName,
            String assessmentType,
            BigDecimal totalScore,
            OffsetDateTime submittedAt
    ) {}
}
