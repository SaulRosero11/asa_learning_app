package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaQuestionRepository extends JpaRepository<QuestionEntity, UUID> {
    List<QuestionEntity> findByAssessmentIdOrderByOrderIndexAsc(UUID assessmentId);
    void deleteByAssessmentId(UUID assessmentId);
}
