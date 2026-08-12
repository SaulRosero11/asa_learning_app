package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface JpaQuestionRepository extends JpaRepository<QuestionEntity, UUID> {
    List<QuestionEntity> findByAssessmentIdOrderByOrderIndexAsc(UUID assessmentId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM question_options WHERE question_id IN (SELECT id FROM questions WHERE assessment_id = :assessmentId)", nativeQuery = true)
    void deleteOptionsByAssessmentId(@Param("assessmentId") UUID assessmentId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM questions WHERE assessment_id = :assessmentId", nativeQuery = true)
    void deleteByAssessmentId(@Param("assessmentId") UUID assessmentId);
}
