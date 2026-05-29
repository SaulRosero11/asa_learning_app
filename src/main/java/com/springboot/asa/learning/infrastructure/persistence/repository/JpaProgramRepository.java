package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.ProgramEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface JpaProgramRepository extends JpaRepository<ProgramEntity, UUID> {

    @Query(
        value = """
            SELECT p.* FROM programs p
            INNER JOIN program_leaders pl ON p.id = pl.program_id
            WHERE pl.user_id = :userId
            """,
        countQuery = """
            SELECT COUNT(*) FROM programs p
            INNER JOIN program_leaders pl ON p.id = pl.program_id
            WHERE pl.user_id = :userId
            """,
        nativeQuery = true
    )
    Page<ProgramEntity> findByLeaderId(@Param("userId") UUID userId, Pageable pageable);

    @Query(
        value = "SELECT p.* FROM programs p INNER JOIN program_leaders pl ON p.id = pl.program_id WHERE pl.user_id = :userId",
        nativeQuery = true
    )
    List<ProgramEntity> findByLeaderId(@Param("userId") UUID userId);

    @Query(
        value = """
            SELECT p.* FROM programs p
            INNER JOIN enrollments e ON p.id = e.program_id
            WHERE e.user_id = :userId AND e.status IN ('ENROLLED', 'COMPLETED')
            """,
        countQuery = """
            SELECT COUNT(*) FROM programs p
            INNER JOIN enrollments e ON p.id = e.program_id
            WHERE e.user_id = :userId AND e.status IN ('ENROLLED', 'COMPLETED')
            """,
        nativeQuery = true
    )
    Page<ProgramEntity> findByStudentId(@Param("userId") UUID userId, Pageable pageable);

    @Query(
        value = """
            SELECT p.* FROM programs p
            INNER JOIN enrollments e ON p.id = e.program_id
            WHERE e.user_id = :userId AND e.status IN ('ENROLLED', 'COMPLETED')
            AND p.status != 'DRAFT'
            """,
        countQuery = """
            SELECT COUNT(*) FROM programs p
            INNER JOIN enrollments e ON p.id = e.program_id
            WHERE e.user_id = :userId AND e.status IN ('ENROLLED', 'COMPLETED')
            AND p.status != 'DRAFT'
            """,
        nativeQuery = true
    )
    Page<ProgramEntity> findActiveByStudentId(@Param("userId") UUID userId, Pageable pageable);

    long countByStatus(String status);

    @Query(
        value = "SELECT COUNT(*) FROM program_leaders WHERE program_id = :programId AND user_id = :userId",
        nativeQuery = true
    )
    int countLeaderAssociation(@Param("programId") UUID programId, @Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(
        value = "INSERT INTO program_leaders (program_id, user_id) VALUES (:programId, :userId) ON CONFLICT DO NOTHING",
        nativeQuery = true
    )
    void addProgramLeader(@Param("programId") UUID programId, @Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(
        value = "DELETE FROM program_leaders WHERE program_id = :programId AND user_id = :userId",
        nativeQuery = true
    )
    int removeProgramLeader(@Param("programId") UUID programId, @Param("userId") UUID userId);

    @Query(
        value = """
            SELECT u.id::text, u.email,
                   up.first_name as firstName, up.last_name as lastName
            FROM program_leaders pl
            JOIN users u ON pl.user_id = u.id
            LEFT JOIN user_profiles up ON u.id = up.user_id
            WHERE pl.program_id = :programId
            """,
        nativeQuery = true
    )
    List<Object[]> findLeadersByProgramId(@Param("programId") UUID programId);
}
