package com.springboot.asa.learning.infrastructure.persistence.adapter;

import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.persistence.entity.UserEntity;
import com.springboot.asa.learning.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements IUserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    /**
     * Para usuarios EXISTENTES: fetchea el entity con sus roles (EAGER) y actualiza
     * solo los campos del dominio — nunca toca user_roles para no borrar asignaciones.
     * Para usuarios NUEVOS: usa el mapper directo.
     */
    @Override
    @Transactional
    public User save(User user) {
        if (user.getId() != null) {
            Optional<UserEntity> existing = jpaUserRepository.findById(user.getId());
            if (existing.isPresent()) {
                UserEntity entity = existing.get();
                entity.setEmail(user.getEmail());
                entity.setActive(user.isActive());
                entity.setAdminEligible(user.isAdminEligible());
                entity.setMustChangePassword(user.isMustChangePassword());
                entity.setOnboardingCompleted(user.isOnboardingCompleted());
                if (user.getPasswordHash() != null) {
                    entity.setPasswordHash(user.getPasswordHash());
                }
                if (user.getAuthProvider() != null) {
                    entity.setAuthProvider(user.getAuthProvider().name());
                }
                return mapper.toDomain(jpaUserRepository.save(entity));
            }
        }
        return mapper.toDomain(jpaUserRepository.save(mapper.toEntity(user)));
    }

    @Override
    public boolean existsByRoleName(String roleName) {
        return jpaUserRepository.existsByRoleName(roleName);
    }
}
