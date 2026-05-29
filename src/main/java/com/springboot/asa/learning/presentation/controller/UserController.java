package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.application.usecase.onboarding.*;
import com.springboot.asa.learning.infrastructure.security.AuthenticatedUser;
import com.springboot.asa.learning.presentation.dto.request.OnboardingRequest;
import com.springboot.asa.learning.presentation.dto.request.UpdateProfileRequest;
import com.springboot.asa.learning.presentation.dto.response.UserProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final CompleteOnboardingUseCaseImpl completeOnboardingUseCase;
    private final GetUserProfileUseCaseImpl getUserProfileUseCase;
    private final UpdateUserProfileUseCaseImpl updateUserProfileUseCase;

    @PostMapping("/onboarding")
    public ResponseEntity<Void> completeOnboarding(
            @Valid @RequestBody OnboardingRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        completeOnboardingUseCase.execute(
                principal.id(),
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.demographicData()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(getUserProfileUseCase.execute(principal.id()));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        updateUserProfileUseCase.execute(
                principal.id(),
                request.firstName(),
                request.lastName(),
                request.phoneNumber()
        );
        return ResponseEntity.noContent().build();
    }
}
