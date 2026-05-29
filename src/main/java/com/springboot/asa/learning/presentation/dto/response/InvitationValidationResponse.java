package com.springboot.asa.learning.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InvitationValidationResponse(
        String email,
        String programName,
        UUID programId,
        String status,
        boolean userExists,
        OffsetDateTime expiresAt
) {}
