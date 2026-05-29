package com.springboot.asa.learning.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProgramResponse(
        UUID id,
        String name,
        String description,
        String status,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID createdBy,
        OffsetDateTime createdAt,
        String imageUrl
) {}
