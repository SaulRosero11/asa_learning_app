package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID programId,
        UUID userId,
        String userEmail,
        String status,
        OffsetDateTime enrolledAt,
        BigDecimal finalGrade
) {}
