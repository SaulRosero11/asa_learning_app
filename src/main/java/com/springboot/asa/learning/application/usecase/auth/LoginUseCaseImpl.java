package com.springboot.asa.learning.application.usecase.auth;

import com.springboot.asa.learning.domain.exception.AccountDisabledException;
import com.springboot.asa.learning.domain.exception.InvalidCredentialsException;
import com.springboot.asa.learning.domain.model.RefreshToken;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IRefreshTokenRepository;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.config.JwtProperties;
import com.springboot.asa.learning.infrastructure.security.JwtService;
import com.springboot.asa.learning.infrastructure.security.TokenHashUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl {

    private final IUserRepository userRepository;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    @Lazy
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User execute(String email, String password, boolean rememberDevice, HttpServletResponse response) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!user.isActive()) throw new AccountDisabledException();

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        emitTokenCookies(user, rememberDevice, response);
        return user;
    }

    public void emitTokenCookies(User user, boolean rememberDevice, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRoles());

        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = TokenHashUtil.sha256(rawRefreshToken);

        long refreshMs = rememberDevice
                ? jwtProperties.getRefreshExpirationLongMs()
                : jwtProperties.getRefreshExpirationShortMs();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashedRefreshToken)
                .expiresAt(OffsetDateTime.now().plusSeconds(refreshMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        int accessMaxAge = (int) (jwtProperties.getAccessExpirationMs() / 1000);
        int refreshMaxAge = (int) (refreshMs / 1000);

        addCookiesToResponse(response, accessToken, rawRefreshToken, accessMaxAge, refreshMaxAge);
    }

    private void addCookiesToResponse(HttpServletResponse response, String accessToken,
                                      String refreshToken, int accessMaxAge, int refreshMaxAge) {
        response.addHeader("Set-Cookie",
                "asa_access_token=" + accessToken +
                        "; HttpOnly; SameSite=Strict; Path=/; Max-Age=" + accessMaxAge);
        response.addHeader("Set-Cookie",
                "asa_refresh_token=" + refreshToken +
                        "; HttpOnly; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=" + refreshMaxAge);
    }
}
