package com.springboot.asa.learning.presentation.dto.response;

import java.util.Set;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        boolean adminEligible,
        boolean active
) {}
