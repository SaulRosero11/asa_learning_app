package com.springboot.asa.learning.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        String email,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt
) {}
