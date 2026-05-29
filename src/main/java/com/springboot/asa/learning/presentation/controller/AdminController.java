package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.application.usecase.auth.ForgotPasswordUseCaseImpl;
import com.springboot.asa.learning.domain.exception.DomainNotEligibleException;
import com.springboot.asa.learning.domain.exception.UserNotFoundException;
import com.springboot.asa.learning.infrastructure.config.AuthProperties;
import com.springboot.asa.learning.infrastructure.persistence.entity.UserEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.*;
import com.springboot.asa.learning.presentation.dto.request.AssignRoleRequest;
import com.springboot.asa.learning.presentation.dto.request.CreateUserRequest;
import com.springboot.asa.learning.presentation.dto.response.AdminStudentResponse;
import com.springboot.asa.learning.presentation.dto.response.AdminUserResponse;
import com.springboot.asa.learning.presentation.dto.response.BeneficiaryProfileResponse;
import com.springboot.asa.learning.presentation.dto.response.ExamSummaryResponse;
import com.springboot.asa.learning.presentation.dto.response.BeneficiaryProfileResponse.EnrollmentSummary;
import com.springboot.asa.learning.presentation.dto.response.BeneficiaryProfileResponse.LeaderProgramSummary;
import com.springboot.asa.learning.presentation.dto.response.BeneficiaryProfileResponse.AttemptSummary;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class AdminController {

    private final JpaUserRepository jpaUserRepository;
    private final JpaUserProfileRepository profileRepository;
    private final JpaRoleRepository jpaRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final ForgotPasswordUseCaseImpl forgotPasswordUseCase;
    private final JpaEnrollmentRepository enrollmentRepository;
    private final JpaAssessmentAttemptRepository attemptRepository;
    private final JpaAssessmentRepository assessmentRepository;
    private final JpaProgramRepository programRepository;

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> listUsers(
            @RequestParam(required = false) Boolean eligible,
            Pageable pageable) {

        Page<UserEntity> users = jpaUserRepository.findNonStudentUsers(pageable);

        Page<AdminUserResponse> response = users.map(u -> {
            var profile = profileRepository.findById(u.getId()).orElse(null);
            return new AdminUserResponse(
                    u.getId(),
                    u.getEmail(),
                    profile != null ? profile.getFirstName() : null,
                    profile != null ? profile.getLastName() : null,
                    u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
                    u.isAdminEligible(),
                    u.isActive()
            );
        });

        return ResponseEntity.ok(response);
    }

    @GetMapping("/students")
    public ResponseEntity<Page<AdminStudentResponse>> listStudents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID programId,
            Pageable pageable) {

        String programIdStr = programId != null ? programId.toString() : null;
        Page<UserEntity> students = jpaUserRepository.findStudents(q, programIdStr, pageable);

        Page<AdminStudentResponse> response = students.map(u -> {
            var profile = profileRepository.findById(u.getId()).orElse(null);
            return new AdminStudentResponse(
                    u.getId(),
                    u.getEmail(),
                    profile != null ? profile.getFirstName() : null,
                    profile != null ? profile.getLastName() : null,
                    u.isOnboardingCompleted(),
                    enrollmentRepository.findByUserId(u.getId()).size(),
                    u.getCreatedAt()
            );
        });

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest request) {

        UserEntity user = jpaUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));

        if (!user.isAdminEligible()) throw new DomainNotEligibleException();

        jpaRoleRepository.findByName(request.roleName()).ifPresent(role -> {
            user.getRoles().add(role);
            jpaUserRepository.save(user);
        });

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> revokeRole(@PathVariable UUID id, @PathVariable String roleName) {
        UserEntity user = jpaUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));

        user.getRoles().removeIf(r -> r.getName().equals(roleName));
        jpaUserRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable UUID id) {
        UserEntity user = jpaUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        forgotPasswordUseCase.execute(user.getEmail());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{id}/profile")
    public ResponseEntity<BeneficiaryProfileResponse> getBeneficiaryProfile(@PathVariable UUID id) {
        UserEntity user = jpaUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        var profile = profileRepository.findById(id).orElse(null);

        List<EnrollmentSummary> enrollments = enrollmentRepository.findByUserId(id).stream()
                .map(e -> {
                    String programName = programRepository.findById(e.getProgramId())
                            .map(p -> p.getName()).orElse("Programa eliminado");
                    return new EnrollmentSummary(
                            e.getProgramId(), programName, e.getStatus(),
                            e.getEnrolledAt(), e.getCompletionDate());
                }).toList();

        List<LeaderProgramSummary> leaderPrograms = programRepository.findByLeaderId(id).stream()
                .map(p -> new LeaderProgramSummary(p.getId(), p.getName()))
                .toList();

        List<AttemptSummary> attempts = attemptRepository
                .findByUserIdAndStatus(id, "SUBMITTED").stream()
                .filter(a -> a.getEndTime() != null)
                .sorted((a, b) -> b.getEndTime().compareTo(a.getEndTime()))
                .limit(50)
                .map(a -> {
                    var assessment = assessmentRepository.findById(a.getAssessmentId());
                    String title = assessment.map(x -> x.getTitle()).orElse("Evaluación");
                    String assessmentType = assessment.map(x -> x.getType()).orElse("EXAM");
                    String programName = assessment
                            .flatMap(x -> programRepository.findById(x.getProgramId()))
                            .map(p -> p.getName()).orElse("Programa");
                    return new AttemptSummary(
                            a.getAssessmentId(), title, programName, assessmentType,
                            a.getTotalScore(), a.getEndTime());
                }).toList();

        return ResponseEntity.ok(new BeneficiaryProfileResponse(
                user.getId(), user.getEmail(),
                profile != null ? profile.getFirstName() : null,
                profile != null ? profile.getLastName() : null,
                profile != null ? profile.getPhoneNumber() : null,
                user.isOnboardingCompleted(),
                enrollments.size(),
                profile != null ? profile.getDemographicData() : java.util.Map.of(),
                enrollments, leaderPrograms, attempts));
    }

    @GetMapping("/students/{id}/exam-summary")
    public ResponseEntity<List<ExamSummaryResponse>> getStudentExamSummary(@PathVariable UUID id) {
        jpaUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado"));

        List<ExamSummaryResponse> summary = attemptRepository.findByUserIdAndStatus(id, "SUBMITTED").stream()
                .collect(Collectors.groupingBy(a -> a.getAssessmentId()))
                .entrySet().stream()
                .map(entry -> {
                    UUID assessmentId = entry.getKey();
                    var attempts = entry.getValue();
                    int attemptCount = attempts.size();
                    BigDecimal bestScore = attempts.stream()
                            .map(a -> a.getTotalScore())
                            .max(Comparator.naturalOrder())
                            .orElse(BigDecimal.ZERO);
                    var assessment = assessmentRepository.findById(assessmentId);
                    String title = assessment.map(x -> x.getTitle()).orElse("Evaluación");
                    String type = assessment.map(x -> x.getType()).orElse("EXAM");
                    return new ExamSummaryResponse(assessmentId, title, type, attemptCount, bestScore);
                })
                .sorted(Comparator.comparing(ExamSummaryResponse::title))
                .toList();

        return ResponseEntity.ok(summary);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
        String email = req.email().trim().toLowerCase();
        String allowedDomain = authProperties.getAllowedAdminDomain();

        if (!email.endsWith("@" + allowedDomain)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El email debe pertenecer al dominio @" + allowedDomain);
        }

        if (jpaUserRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }

        var role = jpaRoleRepository.findByName(req.role())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Rol no válido: " + req.role()));

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setAuthProvider("INTERNAL");
        user.setActive(true);
        user.setAdminEligible(true);
        user.setMustChangePassword(true);
        user.setOnboardingCompleted(false);
        user.setRoles(Set.of(role));

        UserEntity saved = jpaUserRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AdminUserResponse(
                saved.getId(), saved.getEmail(), null, null,
                saved.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
                saved.isAdminEligible(), saved.isActive()));
    }
}
