package com.springboot.asa.learning.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CreateOptionRequest(
        @NotBlank String optionText,
        @JsonProperty("isCorrect") boolean isCorrect
) {}
