package com.springboot.asa.learning.domain.repository;

import com.springboot.asa.learning.domain.model.PasswordResetToken;

import java.util.Optional;

public interface IPasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByTokenHash(String hash);
    void markAsUsed(String hash);
}
