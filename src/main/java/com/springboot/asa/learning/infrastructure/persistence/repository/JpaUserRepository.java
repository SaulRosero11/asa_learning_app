package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName")
    boolean existsByRoleName(@Param("roleName") String roleName);

    @Query(
        value = """
            SELECT DISTINCT u.* FROM users u
            JOIN user_roles ur ON u.id = ur.user_id
            JOIN roles r ON ur.role_id = r.id
            LEFT JOIN user_profiles up ON u.id = up.user_id
            WHERE r.name = 'STUDENT'
            AND (:q IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(COALESCE(up.first_name, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(COALESCE(up.last_name, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:programId IS NULL OR EXISTS (
                SELECT 1 FROM enrollments e WHERE e.user_id = u.id AND e.program_id = CAST(:programId AS uuid)
            ))
            ORDER BY u.created_at DESC
            """,
        countQuery = """
            SELECT COUNT(DISTINCT u.id) FROM users u
            JOIN user_roles ur ON u.id = ur.user_id
            JOIN roles r ON ur.role_id = r.id
            LEFT JOIN user_profiles up ON u.id = up.user_id
            WHERE r.name = 'STUDENT'
            AND (:q IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(COALESCE(up.first_name, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(COALESCE(up.last_name, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:programId IS NULL OR EXISTS (
                SELECT 1 FROM enrollments e WHERE e.user_id = u.id AND e.program_id = CAST(:programId AS uuid)
            ))
            """,
        nativeQuery = true
    )
    Page<UserEntity> findStudents(
            @Param("q") String q,
            @Param("programId") String programId,
            Pageable pageable);

    @Query(
        value = """
            SELECT DISTINCT u.* FROM users u
            JOIN user_roles ur ON u.id = ur.user_id
            JOIN roles r ON ur.role_id = r.id
            WHERE r.name != 'STUDENT'
            """,
        countQuery = """
            SELECT COUNT(DISTINCT u.id) FROM users u
            JOIN user_roles ur ON u.id = ur.user_id
            JOIN roles r ON ur.role_id = r.id
            WHERE r.name != 'STUDENT'
            """,
        nativeQuery = true
    )
    Page<UserEntity> findNonStudentUsers(Pageable pageable);
}
