package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.Valid;

import java.util.List;

public record SubmitAssessmentRequest(
        @Valid List<SubmitAnswerRequest> answers
) {}
