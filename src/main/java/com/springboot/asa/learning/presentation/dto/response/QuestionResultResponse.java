package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record QuestionResultResponse(
        UUID questionId,
        String questionText,
        String questionType,
        BigDecimal pointsAwarded,
        BigDecimal weight,
        UUID selectedOptionId,
        boolean wasCorrect
) {}
