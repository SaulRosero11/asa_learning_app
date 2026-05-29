package com.springboot.asa.learning.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    private UUID id;
    private UUID userId;
    private String tokenHash;
    private OffsetDateTime expiresAt;
    private boolean used;
    private OffsetDateTime createdAt;
}
