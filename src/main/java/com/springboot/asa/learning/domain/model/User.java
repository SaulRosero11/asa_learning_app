package com.springboot.asa.learning.domain.model;

import com.springboot.asa.learning.domain.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String email;
    private String passwordHash;
    private AuthProvider authProvider;
    private boolean active;
    private boolean adminEligible;
    private boolean mustChangePassword;
    private boolean onboardingCompleted;
    private Set<String> roles;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
