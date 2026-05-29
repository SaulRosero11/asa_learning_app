package com.springboot.asa.learning.infrastructure.security;

import com.springboot.asa.learning.application.usecase.auth.LoginUseCaseImpl;
import com.springboot.asa.learning.application.usecase.auth.ProcessGoogleLoginUseCaseImpl;
import com.springboot.asa.learning.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ProcessGoogleLoginUseCaseImpl processGoogleLoginUseCase;
    private final LoginUseCaseImpl loginUseCase;

    @Value("${asa.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name  = oAuth2User.getAttribute("name");

        User user = processGoogleLoginUseCase.execute(email, name);

        // Emitir cookies de sesión (sesión larga por defecto en Google SSO)
        loginUseCase.emitTokenCookies(user, true, response);

        String redirect = user.isOnboardingCompleted()
                ? frontendUrl + "/dashboard"
                : frontendUrl + "/onboarding";
        response.sendRedirect(redirect);
    }
}
