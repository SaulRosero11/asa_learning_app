package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InviteStudentsRequest(
        @NotEmpty(message = "Debe incluir al menos un correo electrónico")
        List<String> emails
) {}
