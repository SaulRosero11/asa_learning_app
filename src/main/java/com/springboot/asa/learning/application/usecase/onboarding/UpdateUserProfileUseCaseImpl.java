package com.springboot.asa.learning.application.usecase.onboarding;

import com.springboot.asa.learning.infrastructure.persistence.entity.UserProfileEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaUserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileUseCaseImpl {

    private final JpaUserProfileRepository profileRepository;

    @Transactional
    public void execute(UUID userId, String firstName, String lastName, String phoneNumber) {
        UserProfileEntity profile = profileRepository.findById(userId)
                .orElseGet(() -> { UserProfileEntity p = new UserProfileEntity(); p.setUserId(userId); return p; });

        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPhoneNumber(phoneNumber);
        profileRepository.save(profile);
    }
}
