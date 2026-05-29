package com.springboot.asa.learning.application.usecase.auth;

import com.springboot.asa.learning.domain.exception.TokenAlreadyUsedException;
import com.springboot.asa.learning.domain.exception.TokenExpiredException;
import com.springboot.asa.learning.domain.exception.UserNotFoundException;
import com.springboot.asa.learning.domain.model.PasswordResetToken;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IPasswordResetTokenRepository;
import com.springboot.asa.learning.domain.repository.IRefreshTokenRepository;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.security.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCaseImpl {

    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IUserRepository userRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(String rawToken, String newPassword) {
        String hash = TokenHashUtil.sha256(rawToken);
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no encontrado"));

        if (token.isUsed()) throw new TokenAlreadyUsedException();
        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) throw new TokenExpiredException();

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UserNotFoundException(token.getUserId().toString()));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.markAsUsed(hash);
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }
}
