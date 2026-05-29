package com.springboot.asa.learning.presentation.dto.response;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        Map<String, Object> demographicData,
        Set<String> roles,
        boolean adminEligible
) {}
