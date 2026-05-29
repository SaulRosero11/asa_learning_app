package com.springboot.asa.learning.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NewsResponse(
        UUID id,
        UUID programId,
        UUID authorId,
        String authorEmail,
        String title,
        String content,
        OffsetDateTime publishedAt
) {}
