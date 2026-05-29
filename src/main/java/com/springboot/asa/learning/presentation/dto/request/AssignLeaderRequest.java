package com.springboot.asa.learning.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignLeaderRequest(@NotNull UUID userId) {}
