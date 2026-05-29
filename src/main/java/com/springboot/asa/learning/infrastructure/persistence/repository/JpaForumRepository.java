package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.ForumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaForumRepository extends JpaRepository<ForumEntity, UUID> {
    Optional<ForumEntity> findByProgramId(UUID programId);
}
