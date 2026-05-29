package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.infrastructure.persistence.entity.*;
import com.springboot.asa.learning.infrastructure.persistence.repository.*;
import com.springboot.asa.learning.infrastructure.security.AuthenticatedUser;
import com.springboot.asa.learning.presentation.dto.request.*;
import com.springboot.asa.learning.presentation.dto.response.LessonResponse;
import com.springboot.asa.learning.presentation.dto.response.ModuleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ModuleController {

    private final JpaModuleRepository moduleRepo;
    private final JpaLessonRepository lessonRepo;
    private final JpaLessonCompletionRepository completionRepo;

    // ─── MODULES ──────────────────────────────────────────────────────────────

    @GetMapping("/programs/{programId}/modules")
    public ResponseEntity<List<ModuleResponse>> listModules(
            @PathVariable UUID programId,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        Set<UUID> completedIds = completionRepo.findCompletedLessonIdsByUserId(principal.id());

        List<ModuleResponse> result = moduleRepo.findByProgramIdOrderByOrderIndexAsc(programId)
                .stream()
                .map(m -> toModuleResponse(m, completedIds))
                .toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/programs/{programId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleResponse createModule(
            @PathVariable UUID programId,
            @Valid @RequestBody CreateModuleRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        ModuleEntity module = new ModuleEntity();
        module.setProgramId(programId);
        module.setTitle(req.title());
        module.setOrderIndex(moduleRepo.countByProgramId(programId));

        return toModuleResponse(moduleRepo.save(module), Set.of());
    }

    @PutMapping("/modules/{id}")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable UUID id,
            @Valid @RequestBody CreateModuleRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        ModuleEntity module = moduleRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Módulo no encontrado"));
        module.setTitle(req.title());

        return ResponseEntity.ok(toModuleResponse(moduleRepo.save(module), Set.of()));
    }

    @PutMapping("/modules/{id}/reorder")
    public ResponseEntity<Void> reorderModule(
            @PathVariable UUID id,
            @Valid @RequestBody ReorderModuleRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        ModuleEntity module = moduleRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Módulo no encontrado"));
        module.setOrderIndex(req.orderIndex());
        moduleRepo.save(module);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/modules/{id}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        if (!moduleRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Módulo no encontrado");
        }
        moduleRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── LESSONS ──────────────────────────────────────────────────────────────

    @PostMapping("/modules/{moduleId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponse createLesson(
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateLessonRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        if (!moduleRepo.existsById(moduleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Módulo no encontrado");
        }

        LessonEntity lesson = new LessonEntity();
        lesson.setModuleId(moduleId);
        lesson.setTitle(req.title());
        lesson.setContentType(req.contentType());
        lesson.setContentUrl(req.contentUrl());
        lesson.setOrderIndex(lessonRepo.countByModuleId(moduleId));

        return toLessonResponse(lessonRepo.save(lesson), false);
    }

    @PutMapping("/lessons/{id}")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLessonRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        LessonEntity lesson = lessonRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lección no encontrada"));
        lesson.setTitle(req.title());
        if (req.contentUrl() != null) lesson.setContentUrl(req.contentUrl());

        return ResponseEntity.ok(toLessonResponse(lessonRepo.save(lesson), false));
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        if (!lessonRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lección no encontrada");
        }
        lessonRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lessons/{id}/complete")
    public ResponseEntity<Void> completeLesson(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        if (!lessonRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lección no encontrada");
        }

        LessonCompletionId completionId = new LessonCompletionId(id, principal.id());
        if (!completionRepo.existsById(completionId)) {
            LessonCompletionEntity completion = new LessonCompletionEntity();
            completion.setId(completionId);
            completion.setCompletedAt(OffsetDateTime.now());
            completionRepo.save(completion);
        }

        return ResponseEntity.noContent().build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ModuleResponse toModuleResponse(ModuleEntity m, Set<UUID> completedLessonIds) {
        List<LessonResponse> lessons = lessonRepo.findByModuleIdOrderByOrderIndexAsc(m.getId())
                .stream()
                .map(l -> toLessonResponse(l, completedLessonIds.contains(l.getId())))
                .toList();

        int completedCount = (int) lessons.stream().filter(LessonResponse::completed).count();

        return new ModuleResponse(
                m.getId(), m.getProgramId(), m.getTitle(), m.getOrderIndex(),
                lessons, lessons.size(), completedCount, m.getCreatedAt());
    }

    private LessonResponse toLessonResponse(LessonEntity l, boolean completed) {
        return new LessonResponse(
                l.getId(), l.getModuleId(), l.getTitle(),
                l.getContentType(), l.getContentUrl(), l.getOrderIndex(), completed);
    }

    private void requireLeaderOrAdmin(AuthenticatedUser principal) {
        var roles = principal.roles();
        if (roles.contains("PROGRAM_LEADER") || roles.contains("ADMIN") || roles.contains("SUPER_ADMIN")) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción");
    }
}
