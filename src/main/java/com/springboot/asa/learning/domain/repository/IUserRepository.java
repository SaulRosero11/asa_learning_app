package com.springboot.asa.learning.domain.repository;

import com.springboot.asa.learning.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface IUserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User user);
    boolean existsByRoleName(String roleName);
}
