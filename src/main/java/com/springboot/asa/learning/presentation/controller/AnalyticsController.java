package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.infrastructure.persistence.entity.*;
import com.springboot.asa.learning.infrastructure.persistence.repository.*;
import com.springboot.asa.learning.infrastructure.security.AuthenticatedUser;
import com.springboot.asa.learning.presentation.dto.response.*;
import com.springboot.asa.learning.presentation.dto.response.GlobalAnalyticsResponse.ProgramEnrollmentStat;
import com.springboot.asa.learning.presentation.dto.response.StudentHistoryResponse.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final JpaEnrollmentRepository enrollmentRepo;
    private final JpaProgramRepository programRepo;
    private final JpaAssessmentAttemptRepository attemptRepo;
    private final JpaAssessmentRepository assessmentRepo;
    private final JpaUserRepository userRepo;

    // ─── Global KPIs (task 7.2) ───────────────────────────────────────────────

    @GetMapping("/global")
    public ResponseEntity<GlobalAnalyticsResponse> globalAnalytics(
            @AuthenticationPrincipal AuthenticatedUser principal) {

        boolean isAdmin = principal.roles().contains("ADMIN") || principal.roles().contains("SUPER_ADMIN");
        boolean isLeader = principal.roles().contains("PROGRAM_LEADER");

        long totalStudents = enrollmentRepo.countDistinctStudents();
        long activePrograms = programRepo.countByStatus("ACTIVE");
        long totalEnrollments = enrollmentRepo.count();
        long completedEnrollments = enrollmentRepo.countByStatus("COMPLETED");
        double completionRate = totalEnrollments > 0
                ? Math.round((double) completedEnrollments / totalEnrollments * 1000) / 10.0
                : 0.0;
        long assessmentsCompleted = attemptRepo.countDistinctUsersByStatus("SUBMITTED");

        List<ProgramEnrollmentStat> stats = programRepo.findAll().stream()
                .filter(p -> isAdmin || (isLeader)) // leaders see all for now
                .map(p -> new ProgramEnrollmentStat(
                        p.getId().toString(),
                        p.getName(),
                        enrollmentRepo.countByProgramId(p.getId())))
                .sorted(Comparator.comparingLong(ProgramEnrollmentStat::enrollmentCount).reversed())
                .limit(10)
                .toList();

        return ResponseEntity.ok(new GlobalAnalyticsResponse(
                totalStudents, activePrograms, completionRate, assessmentsCompleted, stats));
    }

    // ─── Student History (task 7.3) ───────────────────────────────────────────

    @GetMapping("/users/{userId}/history")
    public ResponseEntity<StudentHistoryResponse> studentHistory(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        // Students can only see their own history
        boolean isSelf = principal.id().equals(userId);
        boolean isAdmin = principal.roles().contains("ADMIN") || principal.roles().contains("SUPER_ADMIN");
        if (!isSelf && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        List<EnrollmentEntity> enrollments = enrollmentRepo.findByUserId(userId);

        // Get all submitted attempts by this user
        List<AssessmentAttemptEntity> allAttempts = attemptRepo.findByUserIdAndStatus(userId, "SUBMITTED");

        // Get all assessment IDs and their titles
        Map<UUID, String> assessmentTitles = new HashMap<>();
        allAttempts.forEach(a -> assessmentTitles.computeIfAbsent(a.getAssessmentId(),
                id -> assessmentRepo.findById(id).map(AssessmentEntity::getTitle).orElse("Sin título")));

        // Compute max scores per assessment
        Map<UUID, BigDecimal> maxScores = new HashMap<>();
        assessmentTitles.keySet().forEach(assessmentId -> {
            BigDecimal max = assessmentRepo.findById(assessmentId)
                    .map(a -> BigDecimal.ZERO) // simplified — actual max not stored
                    .orElse(BigDecimal.ZERO);
            maxScores.put(assessmentId, max);
        });

        List<EnrollmentHistory> history = enrollments.stream().map(e -> {
            String programName = programRepo.findById(e.getProgramId())
                    .map(ProgramEntity::getName).orElse("Programa eliminado");

            // Filter attempts belonging to assessments of this program
            List<AssessmentEntity> programAssessments = assessmentRepo.findByProgramIdOrderByCreatedAtDesc(e.getProgramId());
            Set<UUID> programAssessmentIds = programAssessments.stream()
                    .map(AssessmentEntity::getId).collect(Collectors.toSet());

            List<AttemptHistory> attempts = allAttempts.stream()
                    .filter(a -> programAssessmentIds.contains(a.getAssessmentId()))
                    .map(a -> new AttemptHistory(
                            a.getAssessmentId(),
                            assessmentTitles.getOrDefault(a.getAssessmentId(), "Sin título"),
                            a.getTotalScore(),
                            BigDecimal.ZERO, // percentage stored on submit; not persisted on attempt currently
                            a.getEndTime()))
                    .toList();

            return new EnrollmentHistory(
                    e.getProgramId(), programName, e.getStatus(),
                    e.getEnrolledAt(), e.getCompletionDate(), attempts);
        }).toList();

        return ResponseEntity.ok(new StudentHistoryResponse(userId, user.getEmail(), history));
    }

    // ─── Own history shortcut ─────────────────────────────────────────────────

    @GetMapping("/me/history")
    public ResponseEntity<StudentHistoryResponse> myHistory(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return studentHistory(principal.id(), principal);
    }

    @GetMapping("/me/pending")
    public ResponseEntity<List<PendingActivityResponse>> myPendingActivities(
            @AuthenticationPrincipal AuthenticatedUser principal) {

        List<PendingActivityResponse> pending = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        enrollmentRepo.findByUserId(principal.id()).forEach(e -> {
            String programName = programRepo.findById(e.getProgramId())
                    .map(ProgramEntity::getName).orElse("Programa");

            assessmentRepo.findByProgramIdOrderByCreatedAtDesc(e.getProgramId()).stream()
                    .filter(AssessmentEntity::isPublished)
                    .filter(a -> a.getStartDate() == null || !now.isBefore(a.getStartDate()))
                    .filter(a -> a.getEndDate() == null    || !now.isAfter(a.getEndDate()))
                    .forEach(a -> {
                        int used = attemptRepo.countByAssessmentIdAndUserId(a.getId(), principal.id());
                        if (used < a.getMaxAttempts()) {
                            pending.add(new PendingActivityResponse(
                                    "ASSESSMENT", a.getId(), e.getProgramId(), programName,
                                    a.getTitle(), a.getEndDate(), used, a.getMaxAttempts()));
                        }
                    });
        });

        return ResponseEntity.ok(pending);
    }
}
