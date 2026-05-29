package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProgramStatusRequest(
        @NotBlank String status
) {}
