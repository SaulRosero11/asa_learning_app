package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.infrastructure.email.EmailService;
import com.springboot.asa.learning.infrastructure.persistence.entity.*;
import com.springboot.asa.learning.infrastructure.persistence.repository.*;
import com.springboot.asa.learning.infrastructure.security.AuthenticatedUser;
import com.springboot.asa.learning.presentation.dto.request.*;
import com.springboot.asa.learning.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProgramController {

    private final JpaProgramRepository programRepo;
    private final JpaEnrollmentRepository enrollmentRepo;
    private final JpaInvitationRepository invitationRepo;
    private final JpaUserRepository userRepo;
    private final EmailService emailService;

    // ─── PROGRAMS ──────────────────────────────────────────────────────────

    @GetMapping("/programs")
    public ResponseEntity<Page<ProgramResponse>> listPrograms(
            @AuthenticationPrincipal AuthenticatedUser principal,
            Pageable pageable) {

        Page<ProgramEntity> programs;
        var roles = principal.roles();

        if (roles.contains("SUPER_ADMIN") || roles.contains("ADMIN")) {
            programs = programRepo.findAll(pageable);
        } else if (roles.contains("PROGRAM_LEADER")) {
            programs = programRepo.findByLeaderId(principal.id(), pageable);
        } else {
            programs = programRepo.findActiveByStudentId(principal.id(), pageable);
        }

        return ResponseEntity.ok(programs.map(this::toResponse));
    }

    @PostMapping("/programs")
    @ResponseStatus(HttpStatus.CREATED)
    public ProgramResponse createProgram(
            @Valid @RequestBody CreateProgramRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireRole(principal, "PROGRAM_LEADER", "ADMIN", "SUPER_ADMIN");
        validateDateRange(req.startDate(), req.endDate());

        ProgramEntity program = new ProgramEntity();
        program.setName(req.name());
        program.setDescription(req.description());
        program.setStartDate(req.startDate());
        program.setEndDate(req.endDate());
        program.setImageUrl(req.imageUrl());
        program.setStatus("DRAFT");
        program.setCreatedBy(principal.id());

        return toResponse(programRepo.save(program));
    }

    @GetMapping("/programs/{id}")
    public ResponseEntity<ProgramResponse> getProgram(@PathVariable UUID id) {
        return programRepo.findById(id)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Programa no encontrado"));
    }

    @PutMapping("/programs/{id}")
    public ResponseEntity<ProgramResponse> updateProgram(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProgramRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireRole(principal, "PROGRAM_LEADER", "ADMIN", "SUPER_ADMIN");
        validateDateRange(req.startDate(), req.endDate());

        var roles = principal.roles();
        if (roles.contains("PROGRAM_LEADER") && !roles.contains("ADMIN") && !roles.contains("SUPER_ADMIN")) {
            if (programRepo.countLeaderAssociation(id, principal.id()) == 0) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes editar programas que lideras");
            }
        }

        ProgramEntity program = programRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Programa no encontrado"));

        program.setName(req.name());
        program.setDescription(req.description());
        program.setStartDate(req.startDate());
        program.setEndDate(req.endDate());
        program.setImageUrl(req.imageUrl());

        return ResponseEntity.ok(toResponse(programRepo.save(program)));
    }

    @PatchMapping("/programs/{id}/status")
    public ResponseEntity<ProgramResponse> updateProgramStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProgramStatusRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireRole(principal, "PROGRAM_LEADER", "ADMIN", "SUPER_ADMIN");

        ProgramEntity program = programRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Programa no encontrado"));

        var roles = principal.roles();
        if (roles.contains("PROGRAM_LEADER") && !roles.contains("ADMIN") && !roles.contains("SUPER_ADMIN")) {
            if (programRepo.countLeaderAssociation(id, principal.id()) == 0) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes cambiar el estado de programas que lideras");
            }
        }

        String current = program.getStatus();
        String next = "CLOSED".equalsIgnoreCase(req.status()) ? "ARCHIVED" : req.status().toUpperCase();

        boolean valid = ("DRAFT".equals(current) && "ACTIVE".equals(next))
                || ("ACTIVE".equals(current) && "ARCHIVED".equals(next));

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Transición de estado no permitida: " + current + " → " + req.status());
        }

        program.setStatus(next);
        return ResponseEntity.ok(toResponse(programRepo.save(program)));
    }

    @DeleteMapping("/programs/{id}")
    public ResponseEntity<Void> archiveProgram(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireRole(principal, "ADMIN", "SUPER_ADMIN");

        ProgramEntity program = programRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Programa no encontrado"));
        program.setStatus("ARCHIVED");
        programRepo.save(program);
        return ResponseEntity.noContent().build();
    }

    // ─── ENROLLMENTS ───────────────────────────────────────────────────────

    @GetMapping("/programs/{id}/enrollments")
    public ResponseEntity<Page<EnrollmentResponse>> listEnrollments(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal,
            Pageable pageable) {

        requireRole(principal, "PROGRAM_LEADER", "ADMIN", "SUPER_ADMIN");

        Page<EnrollmentEntity> enrollments = enrollmentRepo.findByProgramId(id, pageable);
        return ResponseEntity.ok(enrollments.map(e -> {
            String email = userRepo.findById(e.getUserId())
                    .map(u -> u.getEmail()).orElse("desconocido");
            return new EnrollmentResponse(
                    e.getId(), e.getProgramId(), e.getUserId(),
                    email, e.getStatus(), e.getEnrolledAt(), e.getFinalGrade());
        }));
    }

    // ─── INVITATIONS ───────────────────────────────────────────────────────

    @GetMapping("/programs/{id}/invitations")
    public ResponseEntity<Page<InvitationResponse>> listInvitations(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal,
            Pageable pageable) {

        requireRole(principal, "PROGRAM_LEADER", "ADMIN", "SUPER_ADMIN");

        Page<InvitationResponse> response = invitationRepo
                .findByProgramIdOrderByCreatedAtDesc(id, pageable)
                .map(i -> new InvitationResponse(i.getId(), i.getEmail(),
                        i.getStatus(), i.getCreatedAt(), i.getExpiresAt()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitations/{id}/resend")
    public ResponseEntity<Void> resendInvitation(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireRole(principal, "PROGRAM_LEADER", "ADMIN", "SUPER_ADMIN");

        InvitationEntity invitation = invitationRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitación no encontrada"));

        if ("ACCEPTED".equals(invitation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Esta invitación ya fue aceptada");
        }

        invitation.setStatus("PENDING");
        invitation.setExpiresAt(OffsetDateTime.now().plusDays(7));
        invitationRepo.save(invitation);

        String programName = programRepo.findById(invitation.getProgramId())
                .map(p -> p.getName()).orElse("Programa");
        emailService.sendInvitationEmail(invitation.getEmail(), programName, invitation.getToken());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/programs/{id}/invitations")
    public ResponseEntity<Void> inviteStudents(
            @PathVariable UUID id,
            @Valid @RequestBody InviteStudentsRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireRole(principal, "PROGRAM_LEADER", "ADMIN", "SUPER_ADMIN");

        ProgramEntity program = programRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Programa no encontrado"));

        List<String> emails = req.emails().stream().map(String::trim).distinct().toList();

        for (String email : emails) {
            if (invitationRepo.existsByProgramIdAndEmailAndStatus(id, email, "PENDING")) continue;

            String token = UUID.randomUUID().toString();
            InvitationEntity invitation = new InvitationEntity();
            invitation.setProgramId(id);
            invitation.setEmail(email);
            invitation.setToken(token);
            invitation.setStatus("PENDING");
            invitation.setExpiresAt(OffsetDateTime.now().plusDays(7));
            invitation.setCreatedBy(principal.id());
            invitationRepo.save(invitation);

            emailService.sendInvitationEmail(email, program.getName(), token);
        }

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/invitations/redeem")
    public ResponseEntity<Void> redeemInvitation(
            @Valid @RequestBody RedeemInvitationRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        InvitationEntity invitation = invitationRepo.findByToken(req.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitación no encontrada"));

        if ("ACCEPTED".equals(invitation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Esta invitación ya fue utilizada");
        }

        if ("EXPIRED".equals(invitation.getStatus()) ||
                invitation.getExpiresAt().isBefore(OffsetDateTime.now())) {
            invitationRepo.updateStatusByToken(req.token(), "EXPIRED");
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El enlace de invitación ha expirado");
        }

        ProgramEntity program = programRepo.findById(invitation.getProgramId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Programa no encontrado"));

        if (!"ACTIVE".equals(program.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El programa no está activo");
        }

        if (enrollmentRepo.existsByProgramIdAndUserId(invitation.getProgramId(), principal.id())) {
            invitationRepo.updateStatusByToken(req.token(), "ACCEPTED");
            return ResponseEntity.noContent().build();
        }

        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setProgramId(invitation.getProgramId());
        enrollment.setUserId(principal.id());
        enrollment.setStatus("ENROLLED");
        enrollment.setEnrolledAt(OffsetDateTime.now());
        enrollmentRepo.save(enrollment);

        invitationRepo.updateStatusByToken(req.token(), "ACCEPTED");
        return ResponseEntity.noContent().build();
    }

    // ─── PROGRAM LEADERS ───────────────────────────────────────────────────

    @GetMapping("/programs/{id}/leaders")
    public ResponseEntity<List<LeaderResponse>> getProgramLeaders(@PathVariable UUID id) {
        List<LeaderResponse> leaders = programRepo.findLeadersByProgramId(id).stream()
                .map(row -> new LeaderResponse(
                        UUID.fromString(row[0].toString()),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3]))
                .toList();
        return ResponseEntity.ok(leaders);
    }

    @PostMapping("/programs/{id}/leaders")
    public ResponseEntity<Void> addProgramLeader(
            @PathVariable UUID id,
            @Valid @RequestBody AssignLeaderRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireRole(principal, "ADMIN", "SUPER_ADMIN");

        programRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Programa no encontrado"));

        UserEntity user = userRepo.findById(req.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (!user.isAdminEligible()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El usuario no es elegible para ser líder de programa");
        }

        programRepo.addProgramLeader(id, req.userId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/programs/{id}/leaders/{userId}")
    public ResponseEntity<Void> removeProgramLeader(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireRole(principal, "ADMIN", "SUPER_ADMIN");

        int deleted = programRepo.removeProgramLeader(id, userId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no es líder de este programa");
        }
        return ResponseEntity.noContent().build();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private ProgramResponse toResponse(ProgramEntity p) {
        return new ProgramResponse(p.getId(), p.getName(), p.getDescription(),
                p.getStatus(), p.getStartDate(), p.getEndDate(),
                p.getCreatedBy(), p.getCreatedAt(), p.getImageUrl());
    }

    private void requireRole(AuthenticatedUser principal, String... roles) {
        for (String role : roles) {
            if (principal.roles().contains(role)) return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción");
    }

    private void validateDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }
}
