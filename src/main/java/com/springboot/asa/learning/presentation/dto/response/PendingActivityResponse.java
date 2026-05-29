package com.springboot.asa.learning.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PendingActivityResponse(
        String type,          // "ASSESSMENT" | "LESSON"
        UUID referenceId,     // assessmentId o moduleId
        UUID programId,
        String programName,
        String title,
        OffsetDateTime dueDate,
        int attemptsUsed,
        int maxAttempts
) {}
