package com.springboot.asa.learning.domain.repository;

import com.springboot.asa.learning.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface IRefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByTokenHash(String hash);
    void revokeAllByUserId(UUID userId);
    void revokeByTokenHash(String hash);
}
