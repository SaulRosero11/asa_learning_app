package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.LessonCompletionEntity;
import com.springboot.asa.learning.infrastructure.persistence.entity.LessonCompletionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface JpaLessonCompletionRepository extends JpaRepository<LessonCompletionEntity, LessonCompletionId> {

    @Query("SELECT lc.id.lessonId FROM LessonCompletionEntity lc WHERE lc.id.userId = :userId")
    Set<UUID> findCompletedLessonIdsByUserId(@Param("userId") UUID userId);
}
