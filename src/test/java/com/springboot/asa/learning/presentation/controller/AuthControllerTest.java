package com.springboot.asa.learning.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.asa.learning.application.usecase.auth.LoginUseCaseImpl;
import com.springboot.asa.learning.application.usecase.auth.RefreshTokenUseCaseImpl;
import com.springboot.asa.learning.domain.enums.AuthProvider;
import com.springboot.asa.learning.domain.exception.InvalidCredentialsException;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.infrastructure.security.JwtAuthenticationFilter;
import com.springboot.asa.learning.infrastructure.security.SecurityConfig;
import com.springboot.asa.learning.presentation.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean LoginUseCaseImpl loginUseCase;
    @MockitoBean RefreshTokenUseCaseImpl refreshTokenUseCase;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean com.springboot.asa.learning.application.usecase.auth.ChangePasswordUseCaseImpl changePasswordUseCase;
    @MockitoBean com.springboot.asa.learning.application.usecase.auth.ForgotPasswordUseCaseImpl forgotPasswordUseCase;
    @MockitoBean com.springboot.asa.learning.application.usecase.auth.ResetPasswordUseCaseImpl resetPasswordUseCase;
    @MockitoBean com.springboot.asa.learning.infrastructure.security.OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    @MockitoBean com.springboot.asa.learning.domain.repository.IUserRepository userRepository;

    @Test
    void login_validCredentials_returns200() throws Exception {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("admin@solidaridadyaccion.org")
                .authProvider(AuthProvider.INTERNAL)
                .active(true)
                .mustChangePassword(false)
                .onboardingCompleted(true)
                .roles(Set.of("SUPER_ADMIN"))
                .build();

        given(loginUseCase.execute(anyString(), anyString(), anyBoolean(), any(HttpServletResponse.class)))
                .willReturn(user);

        LoginRequest body = new LoginRequest("admin@solidaridadyaccion.org", "password", false);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@solidaridadyaccion.org"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        given(loginUseCase.execute(anyString(), anyString(), anyBoolean(), any(HttpServletResponse.class)))
                .willThrow(new InvalidCredentialsException());

        LoginRequest body = new LoginRequest("bad@email.com", "wrongpass", false);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
