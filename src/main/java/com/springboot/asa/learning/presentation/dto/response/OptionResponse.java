package com.springboot.asa.learning.presentation.dto.response;

import java.util.UUID;

public record OptionResponse(
        UUID id,
        String optionText,
        Boolean isCorrect
) {}
