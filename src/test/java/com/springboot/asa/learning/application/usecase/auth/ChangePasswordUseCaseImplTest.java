package com.springboot.asa.learning.application.usecase.auth;

import com.springboot.asa.learning.domain.exception.InvalidCredentialsException;
import com.springboot.asa.learning.domain.exception.UserNotFoundException;
import com.springboot.asa.learning.domain.model.User;
import com.springboot.asa.learning.domain.repository.IRefreshTokenRepository;
import com.springboot.asa.learning.domain.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseImplTest {

    @Mock IUserRepository userRepository;
    @Mock IRefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks ChangePasswordUseCaseImpl changePasswordUseCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void execute_normalUser_validCurrentPassword_changesPassword() {
        User user = User.builder().id(userId).passwordHash("$2a$old")
                .mustChangePassword(false).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("currentPass", "$2a$old")).willReturn(true);
        given(passwordEncoder.encode("newPass")).willReturn("$2a$new");
        given(userRepository.save(any())).willReturn(user);

        changePasswordUseCase.execute(userId, "currentPass", "newPass");

        assertThat(user.getPasswordHash()).isEqualTo("$2a$new");
        assertThat(user.isMustChangePassword()).isFalse();
        then(refreshTokenRepository).should().revokeAllByUserId(userId);
    }

    @Test
    void execute_mustChangePasswordUser_skipsCurrentPasswordCheck() {
        User user = User.builder().id(userId).passwordHash("$2a$old")
                .mustChangePassword(true).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.encode("newPass")).willReturn("$2a$new");
        given(userRepository.save(any())).willReturn(user);

        // currentPassword is null — should not throw
        changePasswordUseCase.execute(userId, null, "newPass");

        assertThat(user.isMustChangePassword()).isFalse();
    }

    @Test
    void execute_wrongCurrentPassword_throwsInvalidCredentials() {
        User user = User.builder().id(userId).passwordHash("$2a$old")
                .mustChangePassword(false).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() ->
                changePasswordUseCase.execute(userId, "wrongPass", "newPass"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void execute_userNotFound_throwsUserNotFoundException() {
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                changePasswordUseCase.execute(userId, "any", "newPass"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
