package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.infrastructure.persistence.entity.OnboardingQuestionEntity;
import com.springboot.asa.learning.infrastructure.persistence.repository.JpaOnboardingQuestionRepository;
import com.springboot.asa.learning.presentation.dto.request.OnboardingQuestionRequest;
import com.springboot.asa.learning.presentation.dto.response.OnboardingQuestionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OnboardingController {

    private final JpaOnboardingQuestionRepository questionRepo;

    // ─── PUBLIC (any authenticated user) ──────────────────────────────────────

    @GetMapping("/api/v1/onboarding/questions")
    public ResponseEntity<List<OnboardingQuestionResponse>> getActiveQuestions() {
        return ResponseEntity.ok(
                questionRepo.findByActiveTrueOrderByDisplayOrderAsc()
                        .stream().map(this::toResponse).toList()
        );
    }

    // ─── ADMIN CRUD ───────────────────────────────────────────────────────────

    @GetMapping("/api/v1/admin/onboarding-questions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<OnboardingQuestionResponse>> listAll() {
        return ResponseEntity.ok(
                questionRepo.findAllByOrderByDisplayOrderAsc()
                        .stream().map(this::toResponse).toList()
        );
    }

    @PostMapping("/api/v1/admin/onboarding-questions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OnboardingQuestionResponse> create(
            @Valid @RequestBody OnboardingQuestionRequest req) {

        if (req.fieldKey() == null || req.fieldKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fieldKey es requerido");
        }
        if (req.type() == null || req.type().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type es requerido");
        }
        if (questionRepo.findByFieldKey(req.fieldKey()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una pregunta con esa clave de campo");
        }

        if ("SELECT".equals(req.type()) && (req.options() == null || req.options().size() < 2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las preguntas de tipo SELECT deben tener al menos 2 opciones");
        }

        Integer maxOrder = questionRepo.findMaxDisplayOrder();
        int nextOrder = (maxOrder != null ? maxOrder : 0) + 1;

        OnboardingQuestionEntity entity = new OnboardingQuestionEntity();
        entity.setLabel(req.label());
        entity.setFieldKey(req.fieldKey());
        entity.setType(req.type());
        entity.setOptions(req.options());
        entity.setDisplayOrder(nextOrder);
        entity.setRequired(req.required() != null ? req.required() : true);
        entity.setActive(true);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(questionRepo.save(entity)));
    }

    @PutMapping("/api/v1/admin/onboarding-questions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OnboardingQuestionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody OnboardingQuestionRequest req) {

        OnboardingQuestionEntity entity = questionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pregunta no encontrada"));

        // fieldKey and type are immutable
        entity.setLabel(req.label());
        entity.setOptions(req.options());
        if (req.displayOrder() != null) entity.setDisplayOrder(req.displayOrder());
        entity.setRequired(req.required() != null ? req.required() : entity.isRequired());
        entity.setActive(req.active() != null ? req.active() : entity.isActive());

        return ResponseEntity.ok(toResponse(questionRepo.save(entity)));
    }

    @DeleteMapping("/api/v1/admin/onboarding-questions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        OnboardingQuestionEntity entity = questionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pregunta no encontrada"));

        long usageCount = questionRepo.countProfilesWithFieldKey(entity.getFieldKey());
        if (usageCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: hay " + usageCount +
                            " usuario(s) que respondieron esta pregunta. Desactívala en su lugar.");
        }

        questionRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private OnboardingQuestionResponse toResponse(OnboardingQuestionEntity e) {
        return new OnboardingQuestionResponse(
                e.getId(), e.getLabel(), e.getFieldKey(), e.getType(),
                e.getOptions(), e.getDisplayOrder(), e.isRequired(), e.isActive());
    }
}
