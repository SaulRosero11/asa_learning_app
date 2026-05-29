package com.springboot.asa.learning.presentation.dto.response;

import java.util.UUID;

public record LeaderResponse(UUID id, String email, String firstName, String lastName) {}
