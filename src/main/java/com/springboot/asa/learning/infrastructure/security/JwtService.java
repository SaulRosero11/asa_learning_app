package com.springboot.asa.learning.infrastructure.security;

import com.springboot.asa.learning.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UUID userId, String email, Set<String> roles) {
        SecretKey key = getKey();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessExpirationMs());

        // Guardamos roles como List para que JJWT lo serialice como array JSON estándar
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", List.copyOf(roles))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    /**
     * JJWT 0.12 deserializa arrays JSON como ArrayList, no como Set.
     * Se obtiene como Object y se convierte manualmente a Set<String>.
     */
    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Object rolesObj = getClaims(token).get("roles");

        if (rolesObj instanceof List<?> list) {
            return list.stream()
                    .filter(r -> r instanceof String)
                    .map(r -> (String) r)
                    .collect(Collectors.toSet());
        }

        // Fallback: si ya es Set o algún otro tipo iterable
        if (rolesObj instanceof Set<?> set) {
            return set.stream()
                    .filter(r -> r instanceof String)
                    .map(r -> (String) r)
                    .collect(Collectors.toSet());
        }

        return Set.of();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
