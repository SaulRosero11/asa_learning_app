package com.springboot.asa.learning.application.usecase.auth;

import com.springboot.asa.learning.domain.enums.AuthProvider;
import com.springboot.asa.learning.domain.exception.AccountDisabledException;
import com.springboot.asa.learning.domain.exception.InvalidCredentialsException;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IRefreshTokenRepository;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import com.springboot.asa.learning.infrastructure.config.JwtProperties;
import com.springboot.asa.learning.infrastructure.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock IUserRepository userRepository;
    @Mock IRefreshTokenRepository refreshTokenRepository;
    @Mock JwtService jwtService;
    @Mock JwtProperties jwtProperties;
    @Mock PasswordEncoder passwordEncoder;
    @Mock HttpServletResponse response;

    @InjectMocks LoginUseCaseImpl loginUseCase;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@solidaridadyaccion.org")
                .passwordHash("$2a$hashed")
                .authProvider(AuthProvider.INTERNAL)
                .active(true)
                .roles(Set.of("STUDENT"))
                .build();
    }

    @Test
    void execute_validCredentials_returnsUser() {
        given(userRepository.findByEmail("test@solidaridadyaccion.org"))
                .willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("rawPass", "$2a$hashed")).willReturn(true);
        given(jwtService.generateAccessToken(any(), any(), any())).willReturn("access-token");
        given(jwtProperties.getAccessExpirationMs()).willReturn(900_000L);
        given(jwtProperties.getRefreshExpirationShortMs()).willReturn(1_800_000L);

        User result = loginUseCase.execute("test@solidaridadyaccion.org", "rawPass", false, response);

        assertThat(result.getEmail()).isEqualTo("test@solidaridadyaccion.org");
        then(refreshTokenRepository).should().save(any());
    }

    @Test
    void execute_userNotFound_throwsInvalidCredentials() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                loginUseCase.execute("unknown@email.com", "pass", false, response))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void execute_wrongPassword_throwsInvalidCredentials() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() ->
                loginUseCase.execute("test@solidaridadyaccion.org", "wrongPass", false, response))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void execute_inactiveUser_throwsAccountDisabled() {
        User inactive = User.builder()
                .id(UUID.randomUUID())
                .email("inactive@solidaridadyaccion.org")
                .passwordHash("$2a$hashed")
                .authProvider(AuthProvider.INTERNAL)
                .active(false)
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(inactive));

        assertThatThrownBy(() ->
                loginUseCase.execute("inactive@solidaridadyaccion.org", "pass", false, response))
                .isInstanceOf(AccountDisabledException.class);
    }
}
