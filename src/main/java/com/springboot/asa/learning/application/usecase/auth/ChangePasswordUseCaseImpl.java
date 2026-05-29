package com.springboot.asa.learning.application.usecase.auth;

import com.springboot.asa.learning.domain.exception.InvalidCredentialsException;
import com.springboot.asa.learning.domain.exception.UserNotFoundException;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IRefreshTokenRepository;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCaseImpl {

    private final IUserRepository userRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Si no es primer login obligatorio, validar contraseña actual
        if (!user.isMustChangePassword()) {
            if (currentPassword == null || user.getPasswordHash() == null ||
                    !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                throw new InvalidCredentialsException();
            }
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // Revocar todas las sesiones activas
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
