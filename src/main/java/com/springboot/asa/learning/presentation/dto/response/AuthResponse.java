package com.springboot.asa.learning.presentation.dto.response;

import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        Set<String> roles,
        boolean mustChangePassword,
        boolean onboardingCompleted
) {}
