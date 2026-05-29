package com.springboot.asa.learning.application.usecase.onboarding;

import com.springboot.asa.learning.domain.exception.UserNotFoundException;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.persistence.entity.UserProfileEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaUserProfileRepository;
import com.springboot.asa.learning.presentation.dto.response.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserProfileUseCaseImpl {

    private final IUserRepository userRepository;
    private final JpaUserProfileRepository profileRepository;

    public UserProfileResponse execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        UserProfileEntity profile = profileRepository.findById(userId).orElse(null);

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                profile != null ? profile.getFirstName() : null,
                profile != null ? profile.getLastName() : null,
                profile != null ? profile.getPhoneNumber() : null,
                profile != null ? profile.getDemographicData() : Map.of(),
                user.getRoles(),
                user.isAdminEligible()
        );
    }
}
