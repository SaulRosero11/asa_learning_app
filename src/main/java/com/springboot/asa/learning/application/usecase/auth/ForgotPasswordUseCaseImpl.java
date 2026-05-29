package com.springboot.asa.learning.application.usecase.auth;

import com.springboot.asa.learning.domain.enums.AuthProvider;
import com.springboot.asa.learning.domain.model.PasswordResetToken;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IPasswordResetTokenRepository;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.email.EmailService;
import com.springboot.asa.learning.infrastructure.security.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCaseImpl {

    private final IUserRepository userRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Transactional
    public void execute(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        // Siempre responder igual para no revelar si el email existe
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();

        // No aplica a cuentas Google
        if (AuthProvider.GOOGLE.equals(user.getAuthProvider())) return;

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = TokenHashUtil.sha256(rawToken);

        PasswordResetToken token = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(hashedToken)
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(token);

        emailService.sendPasswordResetEmail(email, null, rawToken);
    }
}
