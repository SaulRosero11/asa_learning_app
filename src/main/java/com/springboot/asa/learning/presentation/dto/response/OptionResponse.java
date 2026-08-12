package com.springboot.asa.learning.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record OptionResponse(
        UUID id,
        String optionText,
        @JsonProperty("isCorrect") Boolean isCorrect
) {}
