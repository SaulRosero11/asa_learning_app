package com.springboot.asa.learning.infrastructure.persistence.adapter;

import com.springboot.asa.learning.domain.model.PasswordResetToken;
import com.springboot.asa.learning.domain.repository.IPasswordResetTokenRepository;
import com.springboot.asa.learning.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaPasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements IPasswordResetTokenRepository {

    private final JpaPasswordResetTokenRepository jpaRepo;

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setUserId(token.getUserId());
        entity.setTokenHash(token.getTokenHash());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsed(token.isUsed());
        PasswordResetTokenEntity saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String hash) {
        return jpaRepo.findByTokenHash(hash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void markAsUsed(String hash) {
        jpaRepo.markAsUsedByHash(hash);
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity e) {
        return PasswordResetToken.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .tokenHash(e.getTokenHash())
                .expiresAt(e.getExpiresAt())
                .used(e.isUsed())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
