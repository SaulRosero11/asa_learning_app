package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaLessonRepository extends JpaRepository<LessonEntity, UUID> {
    List<LessonEntity> findByModuleIdOrderByOrderIndexAsc(UUID moduleId);
    int countByModuleId(UUID moduleId);
}
