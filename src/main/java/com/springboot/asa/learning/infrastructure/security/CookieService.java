package com.springboot.asa.learning.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public void addAccessTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        addHttpOnlyCookie(response, "asa_access_token", token, maxAgeSeconds, "/");
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        addHttpOnlyCookie(response, "asa_refresh_token", token, maxAgeSeconds, "/api/v1/auth/refresh");
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addHttpOnlyCookie(response, "asa_access_token", "", 0, "/");
        addHttpOnlyCookie(response, "asa_refresh_token", "", 0, "/api/v1/auth/refresh");
    }

    private void addHttpOnlyCookie(HttpServletResponse response, String name, String value,
                                   int maxAgeSeconds, String path) {
        boolean secure = !"default".equals(activeProfile) && !"dev".equals(activeProfile);
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
