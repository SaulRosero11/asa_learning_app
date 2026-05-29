package com.springboot.asa.learning.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ForumPostResponse(
        UUID id,
        UUID forumId,
        UUID parentPostId,
        UUID authorId,
        String authorEmail,
        String content,
        boolean deleted,
        OffsetDateTime createdAt,
        List<ForumPostResponse> replies
) {}
