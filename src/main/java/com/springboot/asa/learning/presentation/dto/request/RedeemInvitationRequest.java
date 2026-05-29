package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RedeemInvitationRequest(
        @NotBlank(message = "El token es obligatorio")
        String token
) {}
