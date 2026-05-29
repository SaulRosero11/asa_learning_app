package com.springboot.asa.learning.infrastructure.persistence.mapper;

import com.springboot.asa.learning.domain.enums.AuthProvider;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.infrastructure.persistence.entity.RoleEntity;
import com.springboot.asa.learning.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manual mapper — replaces MapStruct to avoid annotation-processing dependency.
 * Spring registers this as a regular bean (@Component), no apt needed.
 */
@Component
public class UserPersistenceMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .authProvider(mapAuthProvider(entity.getAuthProvider()))
                .active(entity.isActive())
                .adminEligible(entity.isAdminEligible())
                .mustChangePassword(entity.isMustChangePassword())
                .onboardingCompleted(entity.isOnboardingCompleted())
                .roles(mapRoles(entity.getRoles()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;
        UserEntity entity = new UserEntity();
        if (domain.getId() != null) entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setAuthProvider(
                domain.getAuthProvider() != null ? domain.getAuthProvider().name() : "INTERNAL");
        entity.setActive(domain.isActive());
        entity.setAdminEligible(domain.isAdminEligible());
        entity.setMustChangePassword(domain.isMustChangePassword());
        entity.setOnboardingCompleted(domain.isOnboardingCompleted());
        // roles are intentionally NOT mapped here to avoid clearing user_roles table
        return entity;
    }

    private AuthProvider mapAuthProvider(String value) {
        if (value == null) return AuthProvider.INTERNAL;
        try {
            return AuthProvider.valueOf(value);
        } catch (IllegalArgumentException e) {
            return AuthProvider.INTERNAL;
        }
    }

    private Set<String> mapRoles(Set<RoleEntity> roles) {
        if (roles == null || roles.isEmpty()) return Set.of();
        return roles.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
    }
}
