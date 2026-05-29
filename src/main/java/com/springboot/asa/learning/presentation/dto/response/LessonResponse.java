package com.springboot.asa.learning.presentation.dto.response;

import java.util.UUID;

public record LessonResponse(
        UUID id,
        UUID moduleId,
        String title,
        String contentType,
        String contentUrl,
        int orderIndex,
        boolean completed
) {}
