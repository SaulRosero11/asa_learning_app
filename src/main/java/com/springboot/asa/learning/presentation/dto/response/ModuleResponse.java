package com.springboot.asa.learning.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ModuleResponse(
        UUID id,
        UUID programId,
        String title,
        int orderIndex,
        List<LessonResponse> lessons,
        int totalLessons,
        int completedLessons,
        OffsetDateTime createdAt
) {}
