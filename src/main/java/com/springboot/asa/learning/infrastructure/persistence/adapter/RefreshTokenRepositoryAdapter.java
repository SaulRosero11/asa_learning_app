package com.springboot.asa.learning.infrastructure.persistence.adapter;

import com.springboot.asa.learning.domain.model.RefreshToken;
import com.springboot.asa.learning.domain.repository.IRefreshTokenRepository;
import com.springboot.asa.learning.infrastructure.persistence.entity.RefreshTokenEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements IRefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRepo;

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUserId(token.getUserId());
        entity.setTokenHash(token.getTokenHash());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevoked(token.isRevoked());
        RefreshTokenEntity saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String hash) {
        return jpaRepo.findByTokenHash(hash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        jpaRepo.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void revokeByTokenHash(String hash) {
        jpaRepo.revokeByTokenHash(hash);
    }

    private RefreshToken toDomain(RefreshTokenEntity e) {
        return RefreshToken.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .tokenHash(e.getTokenHash())
                .expiresAt(e.getExpiresAt())
                .revoked(e.isRevoked())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
