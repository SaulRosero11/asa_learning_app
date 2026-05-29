package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.StudentResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface JpaStudentResponseRepository extends JpaRepository<StudentResponseEntity, UUID> {
    List<StudentResponseEntity> findByAttemptId(UUID attemptId);

    @Transactional
    void deleteByAttemptIdIn(List<UUID> attemptIds);
}
