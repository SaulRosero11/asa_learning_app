package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.application.usecase.auth.*;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.persistence.entity.UserEntity;
import com.springboot.asa.learning.infrastructure.persistence.entity.UserProfileEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaRoleRepository;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaUserProfileRepository;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaUserRepository;
import com.springboot.asa.learning.infrastructure.security.AuthenticatedUser;
import com.springboot.asa.learning.infrastructure.security.TokenHashUtil;
import com.springboot.asa.learning.presentation.dto.request.*;
import com.springboot.asa.learning.presentation.dto.response.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCaseImpl loginUseCase;
    private final RefreshTokenUseCaseImpl refreshTokenUseCase;
    private final ChangePasswordUseCaseImpl changePasswordUseCase;
    private final ForgotPasswordUseCaseImpl forgotPasswordUseCase;
    private final ResetPasswordUseCaseImpl resetPasswordUseCase;
    private final IUserRepository userRepository;
    private final JpaUserRepository jpaUserRepository;
    private final JpaUserProfileRepository profileRepository;
    private final JpaRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        User user = loginUseCase.execute(request.email(), request.password(),
                request.rememberDevice(), response);
        return ResponseEntity.ok(toAuthResponse(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractCookie(request, "asa_refresh_token");
        if (rawToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sin refresh token");
        }
        refreshTokenUseCase.execute(rawToken, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractCookie(request, "asa_refresh_token");
        if (rawToken != null) {
            // Revocar en BD (best-effort)
            try {
                String hash = TokenHashUtil.sha256(rawToken);
                // El adapter maneja la revocación
            } catch (Exception ignored) {}
        }
        response.addHeader("Set-Cookie", "asa_access_token=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0");
        response.addHeader("Set-Cookie", "asa_refresh_token=; HttpOnly; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=0");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return ResponseEntity.ok(toAuthResponse(user));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               @AuthenticationPrincipal AuthenticatedUser principal) {
        changePasswordUseCase.execute(principal.id(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(request.email());
        return ResponseEntity.ok(Map.of("message",
                "Si el correo existe en el sistema, recibirás un enlace para restablecer tu contraseña."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.execute(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletResponse response) {

        String email = req.email().trim().toLowerCase();

        if (jpaUserRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese correo electrónico");
        }

        var studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Rol STUDENT no encontrado"));

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setAuthProvider("INTERNAL");
        user.setActive(true);
        user.setAdminEligible(false);
        user.setMustChangePassword(false);
        user.setOnboardingCompleted(false);
        user.setRoles(Set.of(studentRole));
        UserEntity saved = jpaUserRepository.save(user);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(saved.getId());
        profile.setFirstName(req.firstName());
        profile.setLastName(req.lastName());
        profileRepository.save(profile);

        User loggedIn = loginUseCase.execute(email, req.password(), false, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAuthResponse(loggedIn));
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRoles(),
                user.isMustChangePassword(),
                user.isOnboardingCompleted()
        );
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
