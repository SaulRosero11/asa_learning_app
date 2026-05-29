package com.springboot.asa.learning.presentation.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        String questionText,
        String questionType,
        BigDecimal weight,
        int orderIndex,
        List<OptionResponse> options
) {}
