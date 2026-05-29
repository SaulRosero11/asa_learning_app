package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.AssessmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaAssessmentRepository extends JpaRepository<AssessmentEntity, UUID> {
    List<AssessmentEntity> findByProgramIdOrderByCreatedAtDesc(UUID programId);
    Page<AssessmentEntity> findByProgramId(UUID programId, Pageable pageable);
}
