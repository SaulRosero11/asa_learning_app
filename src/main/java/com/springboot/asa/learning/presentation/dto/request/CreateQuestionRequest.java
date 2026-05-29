package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public record CreateQuestionRequest(
        @NotBlank String questionText,
        @NotBlank String questionType,
        BigDecimal weight,
        int orderIndex,
        @Valid List<CreateOptionRequest> options
) {}
