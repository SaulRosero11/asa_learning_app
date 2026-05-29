package com.springboot.asa.learning.application.usecase.auth;

import com.springboot.asa.learning.domain.model.RefreshToken;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IRefreshTokenRepository;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.config.JwtProperties;
import com.springboot.asa.learning.infrastructure.security.JwtService;
import com.springboot.asa.learning.infrastructure.security.TokenHashUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUserRepository userRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public void execute(String rawToken, HttpServletResponse response) {
        String hash = TokenHashUtil.sha256(rawToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido"));

        if (stored.isRevoked()) {
            // Detección de reutilización — revocar todas las sesiones del usuario
            refreshTokenRepository.revokeAllByUserId(stored.getUserId());
            clearCookies(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "SESSION_COMPROMISED");
        }

        if (stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            clearCookies(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }

        // Revocar token actual y emitir uno nuevo
        refreshTokenRepository.revokeByTokenHash(hash);

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRoles());
        String newRawRefresh = UUID.randomUUID().toString();
        String newHashedRefresh = TokenHashUtil.sha256(newRawRefresh);

        long expiresInMs = stored.getExpiresAt().toInstant().toEpochMilli() - System.currentTimeMillis();
        RefreshToken newToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(newHashedRefresh)
                .expiresAt(OffsetDateTime.now().plusSeconds(expiresInMs / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newToken);

        int accessMaxAge = (int) (jwtProperties.getAccessExpirationMs() / 1000);
        int refreshMaxAge = (int) (expiresInMs / 1000);

        response.addHeader("Set-Cookie",
                "asa_access_token=" + newAccessToken +
                        "; HttpOnly; SameSite=Strict; Path=/; Max-Age=" + accessMaxAge);
        response.addHeader("Set-Cookie",
                "asa_refresh_token=" + newRawRefresh +
                        "; HttpOnly; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=" + refreshMaxAge);
    }

    private void clearCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", "asa_access_token=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0");
        response.addHeader("Set-Cookie", "asa_refresh_token=; HttpOnly; SameSite=Strict; Path=/api/v1/auth/refresh; Max-Age=0");
    }
}
