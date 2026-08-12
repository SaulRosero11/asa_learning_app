package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.infrastructure.persistence.entity.*;
import com.springboot.asa.learning.infrastructure.persistence.repository.*;
import com.springboot.asa.learning.infrastructure.security.AuthenticatedUser;
import com.springboot.asa.learning.presentation.dto.request.*;
import com.springboot.asa.learning.infrastructure.persistence.entity.AssessmentAttemptEntity;
import com.springboot.asa.learning.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssessmentController {

    private final JpaAssessmentRepository assessmentRepo;
    private final JpaQuestionRepository questionRepo;
    private final JpaAssessmentAttemptRepository attemptRepo;
    private final JpaStudentResponseRepository responseRepo;
    private final JpaUserRepository userRepo;
    private final JpaProgramRepository programRepo;

    // ─── LIST & GET ───────────────────────────────────────────────────────────

    @GetMapping("/programs/{programId}/assessments")
    public ResponseEntity<List<AssessmentResponse>> listAssessments(
            @PathVariable UUID programId,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        boolean isLeader = isLeaderOrAdmin(principal);
        List<AssessmentEntity> all = assessmentRepo.findByProgramIdOrderByCreatedAtDesc(programId);

        // Students see only published assessments within date range
        List<AssessmentEntity> filtered = isLeader ? all : all.stream()
                .filter(AssessmentEntity::isPublished)
                .toList();

        List<AssessmentResponse> result = filtered.stream()
                .map(a -> toAssessmentResponse(a, principal, false))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/assessments/{id}")
    public ResponseEntity<AssessmentResponse> getAssessment(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        AssessmentEntity assessment = findAssessment(id);
        return ResponseEntity.ok(toAssessmentResponse(assessment, principal, true));
    }

    // ─── CRUD (leader) ────────────────────────────────────────────────────────

    @PostMapping("/programs/{programId}/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AssessmentResponse createAssessment(
            @PathVariable UUID programId,
            @Valid @RequestBody CreateAssessmentRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        AssessmentEntity assessment = new AssessmentEntity();
        assessment.setProgramId(programId);
        assessment.setModuleId(req.moduleId());
        assessment.setTitle(req.title());
        assessment.setType(req.type() != null ? req.type() : "EXAM");
        assessment.setStartDate(req.startDate());
        assessment.setEndDate(req.endDate());
        assessment.setMaxAttempts(req.maxAttempts() > 0 ? req.maxAttempts() : 1);
        assessment.setPublished(req.published());
        assessment = assessmentRepo.save(assessment);

        if (req.questions() != null) {
            int idx = 0;
            for (CreateQuestionRequest qReq : req.questions()) {
                QuestionEntity question = new QuestionEntity();
                question.setAssessmentId(assessment.getId());
                question.setQuestionText(qReq.questionText());
                question.setQuestionType(qReq.questionType() != null ? qReq.questionType() : "SINGLE_CHOICE");
                question.setWeight(qReq.weight() != null ? qReq.weight() : BigDecimal.ZERO);
                question.setOrderIndex(qReq.orderIndex() > 0 ? qReq.orderIndex() : idx);
                idx++;

                // Save first to generate UUID, then attach options with questionId set
                question = questionRepo.save(question);

                if (qReq.options() != null && !qReq.options().isEmpty()) {
                    List<QuestionOptionEntity> options = new ArrayList<>();
                    for (CreateOptionRequest oReq : qReq.options()) {
                        QuestionOptionEntity option = new QuestionOptionEntity();
                        option.setQuestionId(question.getId());
                        option.setOptionText(oReq.optionText());
                        option.setCorrect(oReq.isCorrect());
                        options.add(option);
                    }
                    question.setOptions(options);
                    questionRepo.save(question);
                }
            }
        }

        return toAssessmentResponse(assessment, principal, false);
    }

    @PutMapping("/assessments/{id}")
    @Transactional
    public ResponseEntity<AssessmentResponse> updateAssessment(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAssessmentRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);
        AssessmentEntity assessment = findAssessment(id);

        assessment.setTitle(req.title());
        if (req.type() != null) assessment.setType(req.type());
        assessment.setStartDate(req.startDate());
        assessment.setEndDate(req.endDate());
        if (req.maxAttempts() > 0) assessment.setMaxAttempts(req.maxAttempts());
        assessment.setPublished(req.published());
        assessment.setModuleId(req.moduleId());
        assessmentRepo.save(assessment);

        if (req.questions() != null) {
            // Delete existing attempts (and their responses) before replacing questions
            List<UUID> attemptIds = attemptRepo.findIdsByAssessmentId(id);
            if (!attemptIds.isEmpty()) {
                responseRepo.deleteByAttemptIdIn(attemptIds);
                attemptRepo.deleteByAssessmentId(id);
            }
            // Delete options first (native SQL avoids JPA orphan-removal FK nullification)
            questionRepo.deleteOptionsByAssessmentId(id);
            // Replace all questions
            questionRepo.deleteByAssessmentId(id);
            int idx = 0;
            for (CreateQuestionRequest qReq : req.questions()) {
                QuestionEntity question = new QuestionEntity();
                question.setAssessmentId(id);
                question.setQuestionText(qReq.questionText());
                question.setQuestionType(qReq.questionType() != null ? qReq.questionType() : "SINGLE_CHOICE");
                question.setWeight(qReq.weight() != null ? qReq.weight() : BigDecimal.ZERO);
                question.setOrderIndex(qReq.orderIndex() > 0 ? qReq.orderIndex() : idx);
                idx++;

                question = questionRepo.save(question);

                if (qReq.options() != null && !qReq.options().isEmpty()) {
                    List<QuestionOptionEntity> options = new ArrayList<>();
                    for (CreateOptionRequest oReq : qReq.options()) {
                        QuestionOptionEntity option = new QuestionOptionEntity();
                        option.setQuestionId(question.getId());
                        option.setOptionText(oReq.optionText());
                        option.setCorrect(oReq.isCorrect());
                        options.add(option);
                    }
                    question.setOptions(options);
                    questionRepo.save(question);
                }
            }
        }

        return ResponseEntity.ok(toAssessmentResponse(assessment, principal, false));
    }

    @DeleteMapping("/assessments/{id}")
    public ResponseEntity<Void> deleteAssessment(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);
        if (!assessmentRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluación no encontrada");
        }
        assessmentRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── SUBMIT (task 5.4) ────────────────────────────────────────────────────

    @PostMapping("/assessments/{id}/submit")
    @Transactional
    public ResponseEntity<AttemptResultResponse> submitAssessment(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitAssessmentRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        AssessmentEntity assessment = findAssessment(id);

        if (!assessment.isPublished()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "La evaluación no está publicada");
        }
        if (assessment.getStartDate() != null && OffsetDateTime.now().isBefore(assessment.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "La evaluación aún no ha comenzado");
        }
        if (assessment.getEndDate() != null && OffsetDateTime.now().isAfter(assessment.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "El período de la evaluación ya finalizó");
        }

        int attemptsUsed = attemptRepo.countByAssessmentIdAndUserId(id, principal.id());
        if (attemptsUsed >= assessment.getMaxAttempts()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Has agotado todos tus intentos disponibles");
        }

        // Create attempt
        AssessmentAttemptEntity attempt = new AssessmentAttemptEntity();
        attempt.setAssessmentId(id);
        attempt.setUserId(principal.id());
        attempt.setAttemptNumber(attemptsUsed + 1);
        attempt.setStartTime(OffsetDateTime.now());
        attempt.setStatus("IN_PROGRESS");
        attempt = attemptRepo.save(attempt);

        // Score calculation
        List<QuestionEntity> questions = questionRepo.findByAssessmentIdOrderByOrderIndexAsc(id);
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = questions.stream()
                .map(QuestionEntity::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<QuestionResultResponse> questionResults = new ArrayList<>();

        Map<UUID, SubmitAnswerRequest> answerMap = new HashMap<>();
        if (req.answers() != null) {
            for (SubmitAnswerRequest a : req.answers()) {
                if (a.questionId() != null) answerMap.put(a.questionId(), a);
            }
        }

        for (QuestionEntity question : questions) {
            SubmitAnswerRequest answer = answerMap.get(question.getId());
            BigDecimal points = BigDecimal.ZERO;
            UUID selectedOptionId = null;
            String textResponse = null;
            boolean wasCorrect = false;

            if (answer != null) {
                selectedOptionId = answer.selectedOptionId();
                textResponse = answer.textResponse();
                List<QuestionOptionEntity> options = question.getOptions();

                if ("SINGLE_CHOICE".equals(question.getQuestionType()) && answer.selectedOptionId() != null) {
                    wasCorrect = options.stream()
                            .anyMatch(o -> o.getId().equals(answer.selectedOptionId()) && o.isCorrect());
                    points = wasCorrect ? question.getWeight() : BigDecimal.ZERO;

                } else if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
                    Set<UUID> selected = answer.selectedOptionIds() != null
                            ? new HashSet<>(answer.selectedOptionIds()) : Set.of();
                    Set<UUID> correct = options.stream()
                            .filter(QuestionOptionEntity::isCorrect)
                            .map(QuestionOptionEntity::getId)
                            .collect(Collectors.toSet());
                    wasCorrect = selected.equals(correct);
                    points = wasCorrect ? question.getWeight() : BigDecimal.ZERO;
                }
                // TEXT_OPEN: points = 0 (manual grading out of scope for MVP)
            }

            totalScore = totalScore.add(points);

            StudentResponseEntity studentResponse = new StudentResponseEntity();
            studentResponse.setAttemptId(attempt.getId());
            studentResponse.setQuestionId(question.getId());
            studentResponse.setSelectedOptionId(selectedOptionId);
            studentResponse.setTextResponse(textResponse);
            studentResponse.setPointsAwarded(points);
            responseRepo.save(studentResponse);

            questionResults.add(new QuestionResultResponse(
                    question.getId(), question.getQuestionText(), question.getQuestionType(),
                    points, question.getWeight(), selectedOptionId, wasCorrect));
        }

        BigDecimal percentage = maxScore.compareTo(BigDecimal.ZERO) > 0
                ? totalScore.divide(maxScore, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        attempt.setTotalScore(totalScore);
        attempt.setEndTime(OffsetDateTime.now());
        attempt.setStatus("SUBMITTED");
        attemptRepo.save(attempt);

        return ResponseEntity.ok(new AttemptResultResponse(
                attempt.getId(),
                totalScore,
                maxScore,
                percentage,
                "SUBMITTED",
                attempt.getEndTime(),
                "EXAM".equals(assessment.getType()) ? questionResults : null));
    }

    // Student: see own attempts
    @GetMapping("/assessments/{id}/my-attempts")
    public ResponseEntity<List<AttemptResultResponse>> getMyAttempts(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        AssessmentEntity assessment = findAssessment(id);
        BigDecimal maxScore = questionRepo.findByAssessmentIdOrderByOrderIndexAsc(id).stream()
                .map(QuestionEntity::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AttemptResultResponse> attempts = attemptRepo
                .findByAssessmentIdAndUserIdOrderByAttemptNumberDesc(id, principal.id())
                .stream()
                .map(attempt -> {
                    BigDecimal pct = maxScore.compareTo(BigDecimal.ZERO) > 0
                            ? attempt.getTotalScore()
                                    .divide(maxScore, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new AttemptResultResponse(
                            attempt.getId(), attempt.getTotalScore(),
                            maxScore, pct, attempt.getStatus(),
                            attempt.getEndTime(), null);
                })
                .toList();

        return ResponseEntity.ok(attempts);
    }

    // ─── ALL ATTEMPTS (for leaders/admins) ───────────────────────────────────

    @GetMapping("/assessments/{id}/attempts/all")
    public ResponseEntity<List<AttemptDetailResponse>> getAllAttempts(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        AssessmentEntity assessment = findAssessment(id);

        // Leaders can only access their own programs' assessments
        if (!principal.roles().contains("ADMIN") && !principal.roles().contains("SUPER_ADMIN")) {
            if (programRepo.countLeaderAssociation(assessment.getProgramId(), principal.id()) == 0) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No tienes permiso para ver los resultados de esta evaluación");
            }
        }

        // Load questions once for lookup
        Map<UUID, QuestionEntity> questions = questionRepo
                .findByAssessmentIdOrderByOrderIndexAsc(id).stream()
                .collect(Collectors.toMap(QuestionEntity::getId, q -> q));

        Map<UUID, QuestionOptionEntity> options = questions.values().stream()
                .flatMap(q -> q.getOptions().stream())
                .collect(Collectors.toMap(QuestionOptionEntity::getId, o -> o));

        List<AttemptDetailResponse> result = attemptRepo
                .findByAssessmentIdOrderByStartTimeDesc(id,
                        org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(a -> "SUBMITTED".equals(a.getStatus()))
                .map(attempt -> {
                    String email = userRepo.findById(attempt.getUserId())
                            .map(u -> u.getEmail()).orElse("desconocido");

                    List<AttemptDetailResponse.AnswerDetail> answers =
                            responseRepo.findByAttemptId(attempt.getId()).stream()
                                    .map(r -> {
                                        QuestionEntity q = questions.get(r.getQuestionId());
                                        String qText = q != null ? q.getQuestionText() : "Pregunta";
                                        String optText = r.getSelectedOptionId() != null
                                                ? options.getOrDefault(r.getSelectedOptionId(),
                                                        new QuestionOptionEntity()).getOptionText()
                                                : r.getTextResponse();
                                        boolean correct = r.getPointsAwarded() != null
                                                && r.getPointsAwarded().compareTo(BigDecimal.ZERO) > 0;
                                        return new AttemptDetailResponse.AnswerDetail(
                                                r.getId(), r.getQuestionId(), qText,
                                                q != null ? q.getQuestionType() : "SINGLE_CHOICE",
                                                optText != null ? optText : "—", correct,
                                                r.getPointsAwarded() != null ? r.getPointsAwarded() : BigDecimal.ZERO,
                                                q != null ? q.getWeight() : BigDecimal.ZERO);
                                    }).toList();

                    return new AttemptDetailResponse(
                            attempt.getId(), attempt.getUserId(), email,
                            attempt.getTotalScore(), attempt.getEndTime(), answers);
                }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/assessments/{id}/results")
    public ResponseEntity<List<UserResultSummary>> getResultsByUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);
        AssessmentEntity assessment = findAssessment(id);

        if (!principal.roles().contains("ADMIN") && !principal.roles().contains("SUPER_ADMIN")) {
            if (programRepo.countLeaderAssociation(assessment.getProgramId(), principal.id()) == 0) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No tienes permiso para ver los resultados de esta evaluación");
            }
        }

        Map<UUID, QuestionEntity> questions = questionRepo
                .findByAssessmentIdOrderByOrderIndexAsc(id).stream()
                .collect(Collectors.toMap(QuestionEntity::getId, q -> q));

        Map<UUID, QuestionOptionEntity> options = questions.values().stream()
                .flatMap(q -> q.getOptions().stream())
                .collect(Collectors.toMap(QuestionOptionEntity::getId, o -> o));

        // Group submitted attempts by userId
        Map<UUID, List<AssessmentAttemptEntity>> attemptsByUser = attemptRepo
                .findByAssessmentIdOrderByStartTimeDesc(id,
                        org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(a -> "SUBMITTED".equals(a.getStatus()))
                .collect(Collectors.groupingBy(AssessmentAttemptEntity::getUserId));

        List<UserResultSummary> results = attemptsByUser.entrySet().stream().map(entry -> {
            UUID userId = entry.getKey();
            List<AssessmentAttemptEntity> userAttempts = entry.getValue();

            String email = userRepo.findById(userId).map(u -> u.getEmail()).orElse("desconocido");

            BigDecimal bestScore = "SURVEY".equals(assessment.getType()) ? null :
                    userAttempts.stream()
                            .map(AssessmentAttemptEntity::getTotalScore)
                            .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

            OffsetDateTime lastAttemptAt = userAttempts.stream()
                    .map(AssessmentAttemptEntity::getEndTime)
                    .filter(t -> t != null)
                    .max(OffsetDateTime::compareTo).orElse(null);

            List<AttemptDetailResponse> attemptDetails = userAttempts.stream().map(attempt -> {
                List<AttemptDetailResponse.AnswerDetail> answers =
                        responseRepo.findByAttemptId(attempt.getId()).stream()
                                .map(r -> {
                                    QuestionEntity q = questions.get(r.getQuestionId());
                                    String qText = q != null ? q.getQuestionText() : "Pregunta";
                                    String optText = r.getSelectedOptionId() != null
                                            ? options.getOrDefault(r.getSelectedOptionId(),
                                                    new QuestionOptionEntity()).getOptionText()
                                            : r.getTextResponse();
                                    boolean correct = r.getPointsAwarded() != null
                                            && r.getPointsAwarded().compareTo(BigDecimal.ZERO) > 0;
                                    return new AttemptDetailResponse.AnswerDetail(
                                            r.getId(), r.getQuestionId(), qText,
                                            q != null ? q.getQuestionType() : "SINGLE_CHOICE",
                                            optText != null ? optText : "—", correct,
                                            r.getPointsAwarded() != null ? r.getPointsAwarded() : BigDecimal.ZERO,
                                            q != null ? q.getWeight() : BigDecimal.ZERO);
                                }).toList();
                return new AttemptDetailResponse(
                        attempt.getId(), attempt.getUserId(), email,
                        attempt.getTotalScore(), attempt.getEndTime(), answers);
            }).toList();

            return new UserResultSummary(userId, email, assessment.getType(),
                    userAttempts.size(), bestScore, lastAttemptAt, attemptDetails);
        }).sorted(Comparator.comparing(UserResultSummary::lastAttemptAt,
                Comparator.nullsLast(Comparator.reverseOrder()))).toList();

        return ResponseEntity.ok(results);
    }

    // ─── Manual grading ───────────────────────────────────────────────────────

    @PutMapping("/responses/{responseId}/grade")
    @Transactional
    public ResponseEntity<Map<String, BigDecimal>> gradeResponse(
            @PathVariable UUID responseId,
            @RequestBody Map<String, BigDecimal> body,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        StudentResponseEntity response = responseRepo.findById(responseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada"));

        BigDecimal points = body.get("points");
        if (points == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo 'points' es requerido");
        }

        QuestionEntity question = questionRepo.findById(response.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregunta no encontrada"));

        if (points.compareTo(BigDecimal.ZERO) < 0 || points.compareTo(question.getWeight()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los puntos deben estar entre 0 y " + question.getWeight());
        }

        response.setPointsAwarded(points);
        responseRepo.save(response);

        AssessmentAttemptEntity attempt = attemptRepo.findById(response.getAttemptId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Intento no encontrado"));

        BigDecimal newTotal = responseRepo.findByAttemptId(attempt.getId()).stream()
                .map(r -> r.getPointsAwarded() != null ? r.getPointsAwarded() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        attempt.setTotalScore(newTotal);
        attemptRepo.save(attempt);

        return ResponseEntity.ok(Map.of("totalScore", newTotal));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private AssessmentEntity findAssessment(UUID id) {
        return assessmentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evaluación no encontrada"));
    }

    private AssessmentResponse toAssessmentResponse(AssessmentEntity a,
                                                     AuthenticatedUser principal,
                                                     boolean includeQuestions) {
        boolean isLeader = isLeaderOrAdmin(principal);
        int attemptsUsed = attemptRepo.countByAssessmentIdAndUserId(a.getId(), principal.id());

        List<QuestionResponse> questions = null;
        if (includeQuestions) {
            questions = questionRepo.findByAssessmentIdOrderByOrderIndexAsc(a.getId())
                    .stream()
                    .map(q -> toQuestionResponse(q, isLeader))
                    .toList();
        }

        return new AssessmentResponse(a.getId(), a.getProgramId(), a.getModuleId(),
                a.getTitle(), a.getType(), a.isPublished(),
                a.getStartDate(), a.getEndDate(), a.getMaxAttempts(),
                attemptsUsed, questions, a.getCreatedAt());
    }

    private QuestionResponse toQuestionResponse(QuestionEntity q, boolean showCorrect) {
        List<OptionResponse> options = q.getOptions().stream()
                .map(o -> new OptionResponse(
                        o.getId(),
                        o.getOptionText(),
                        showCorrect ? o.isCorrect() : null))
                .toList();
        return new QuestionResponse(q.getId(), q.getQuestionText(), q.getQuestionType(),
                q.getWeight(), q.getOrderIndex(), options);
    }

    private boolean isLeaderOrAdmin(AuthenticatedUser p) {
        var r = p.roles();
        return r.contains("PROGRAM_LEADER") || r.contains("ADMIN") || r.contains("SUPER_ADMIN");
    }

    private void requireLeaderOrAdmin(AuthenticatedUser p) {
        if (!isLeaderOrAdmin(p)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción");
        }
    }
}
