package com.springboot.asa.learning.application.usecase.auth;

import com.springboot.asa.learning.domain.enums.AuthProvider;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaRoleRepository;
import com.springboot.asa.learning.infrastructure.persistence.entity.UserEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaUserRepository;
import com.springboot.asa.learning.infrastructure.security.DomainEligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProcessGoogleLoginUseCaseImpl {

    private final IUserRepository userRepository;
    private final JpaUserRepository jpaUserRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final DomainEligibilityService domainEligibilityService;

    @Transactional
    public User execute(String email, String name) {
        return userRepository.findByEmail(email)
                .map(existing -> updateExistingForGoogle(existing, email))
                .orElseGet(() -> createNewGoogleUser(email));
    }

    private User updateExistingForGoogle(User user, String email) {
        if (!AuthProvider.GOOGLE.equals(user.getAuthProvider())) {
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setAdminEligible(domainEligibilityService.isEligible(email));
            return userRepository.save(user);
        }
        return user;
    }

    private User createNewGoogleUser(String email) {
        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setAuthProvider(AuthProvider.GOOGLE.name());
        entity.setActive(true);
        entity.setAdminEligible(domainEligibilityService.isEligible(email));
        entity.setMustChangePassword(false);
        entity.setOnboardingCompleted(false);

        jpaRoleRepository.findByName("STUDENT")
                .ifPresent(role -> entity.setRoles(Set.of(role)));

        UserEntity saved = jpaUserRepository.save(entity);

        return User.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .authProvider(AuthProvider.GOOGLE)
                .active(saved.isActive())
                .adminEligible(saved.isAdminEligible())
                .mustChangePassword(false)
                .onboardingCompleted(false)
                .roles(Set.of("STUDENT"))
                .build();
    }
}
