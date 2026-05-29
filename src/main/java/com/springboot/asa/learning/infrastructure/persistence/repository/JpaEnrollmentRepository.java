package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.EnrollmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaEnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {
    Page<EnrollmentEntity> findByProgramId(UUID programId, Pageable pageable);
    boolean existsByProgramIdAndUserId(UUID programId, UUID userId);
    Optional<EnrollmentEntity> findByProgramIdAndUserId(UUID programId, UUID userId);
    List<EnrollmentEntity> findByUserId(UUID userId);
    long countByProgramId(UUID programId);
    long countByStatus(String status);

    @Query("SELECT COUNT(DISTINCT e.userId) FROM EnrollmentEntity e")
    long countDistinctStudents();
}
