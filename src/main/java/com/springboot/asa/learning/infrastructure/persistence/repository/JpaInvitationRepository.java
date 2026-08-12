package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.InvitationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface JpaInvitationRepository extends JpaRepository<InvitationEntity, UUID> {
    Optional<InvitationEntity> findByToken(String token);
    boolean existsByProgramIdAndEmailAndStatus(UUID programId, String email, String status);
    Page<InvitationEntity> findByProgramIdOrderByCreatedAtDesc(UUID programId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE InvitationEntity i SET i.status = :status WHERE i.token = :token")
    void updateStatusByToken(@Param("token") String token, @Param("status") String status);
}
