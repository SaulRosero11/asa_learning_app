package com.springboot.asa.learning.application.usecase.onboarding;

import com.springboot.asa.learning.domain.exception.UserNotFoundException;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.persistence.entity.UserProfileEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaUserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompleteOnboardingUseCaseImpl {

    private final IUserRepository userRepository;
    private final JpaUserProfileRepository profileRepository;

    @Transactional
    public void execute(UUID userId, String firstName, String lastName,
                        String phoneNumber, Map<String, Object> demographicData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Crear o actualizar perfil
        UserProfileEntity profile = profileRepository.findById(userId)
                .orElseGet(() -> { UserProfileEntity p = new UserProfileEntity(); p.setUserId(userId); return p; });

        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPhoneNumber(phoneNumber);
        profile.setDemographicData(demographicData);
        profileRepository.save(profile);

        // Marcar onboarding como completado
        user.setOnboardingCompleted(true);
        userRepository.save(user);
    }
}
