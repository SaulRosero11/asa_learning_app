package com.springboot.asa.learning.infrastructure.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID id, String email, Set<String> roles) {}
