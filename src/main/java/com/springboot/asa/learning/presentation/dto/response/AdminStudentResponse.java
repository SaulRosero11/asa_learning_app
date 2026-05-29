package com.springboot.asa.learning.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminStudentResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        boolean onboardingCompleted,
        int enrollmentCount,
        OffsetDateTime createdAt
) {}
