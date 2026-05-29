package com.springboot.asa.learning.presentation.dto.request;

import java.util.List;
import java.util.UUID;

public record SubmitAnswerRequest(
        UUID questionId,
        UUID selectedOptionId,
        List<UUID> selectedOptionIds,
        String textResponse
) {}
