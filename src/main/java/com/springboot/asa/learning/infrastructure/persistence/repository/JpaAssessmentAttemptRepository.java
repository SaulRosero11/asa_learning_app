package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.AssessmentAttemptEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaAssessmentAttemptRepository extends JpaRepository<AssessmentAttemptEntity, UUID> {
    int countByAssessmentIdAndUserId(UUID assessmentId, UUID userId);
    Page<AssessmentAttemptEntity> findByAssessmentIdOrderByStartTimeDesc(UUID assessmentId, Pageable pageable);
    List<AssessmentAttemptEntity> findByAssessmentIdAndUserIdOrderByAttemptNumberDesc(UUID assessmentId, UUID userId);
    Optional<AssessmentAttemptEntity> findTopByAssessmentIdAndUserIdOrderByAttemptNumberDesc(UUID assessmentId, UUID userId);
    long countByStatus(String status);

    @Query("SELECT COUNT(DISTINCT a.userId) FROM AssessmentAttemptEntity a WHERE a.status = :status")
    long countDistinctUsersByStatus(@Param("status") String status);

    @Query("SELECT a.id FROM AssessmentAttemptEntity a WHERE a.assessmentId = :assessmentId")
    List<UUID> findIdsByAssessmentId(@Param("assessmentId") UUID assessmentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM AssessmentAttemptEntity a WHERE a.assessmentId = :assessmentId")
    void deleteByAssessmentId(@Param("assessmentId") UUID assessmentId);
    List<AssessmentAttemptEntity> findByUserIdAndStatus(UUID userId, String status);
}
