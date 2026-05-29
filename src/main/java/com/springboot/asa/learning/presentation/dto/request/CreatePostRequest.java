package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreatePostRequest(
        @NotBlank String content,
        UUID parentPostId
) {}
