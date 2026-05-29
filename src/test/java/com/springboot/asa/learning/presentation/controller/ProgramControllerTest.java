package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.infrastructure.persistence.entity.ProgramEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.*;
import com.springboot.asa.learning.infrastructure.security.AuthenticatedUser;
import com.springboot.asa.learning.infrastructure.security.JwtAuthenticationFilter;
import com.springboot.asa.learning.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProgramController.class)
@Import(SecurityConfig.class)
class ProgramControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean JpaProgramRepository programRepo;
    @MockitoBean JpaEnrollmentRepository enrollmentRepo;
    @MockitoBean JpaInvitationRepository invitationRepo;
    @MockitoBean JpaUserRepository userRepo;
    @MockitoBean com.springboot.asa.learning.infrastructure.email.EmailService emailService;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean com.springboot.asa.learning.infrastructure.security.OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    private UsernamePasswordAuthenticationToken adminAuth() {
        AuthenticatedUser principal = new AuthenticatedUser(
                UUID.randomUUID(), "admin@solidaridadyaccion.org", Set.of("SUPER_ADMIN"));
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
    }

    @Test
    void listPrograms_authenticated_returns200() throws Exception {
        ProgramEntity program = new ProgramEntity();
        program.setId(UUID.randomUUID());
        program.setName("Programa Test");
        program.setStatus("ACTIVE");
        program.setCreatedAt(OffsetDateTime.now());
        program.setUpdatedAt(OffsetDateTime.now());

        given(programRepo.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(program)));

        mockMvc.perform(get("/api/v1/programs")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Programa Test"));
    }

    @Test
    void listPrograms_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/programs"))
                .andExpect(status().isUnauthorized());
    }
}
