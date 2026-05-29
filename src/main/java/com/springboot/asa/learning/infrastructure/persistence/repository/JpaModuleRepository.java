package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaModuleRepository extends JpaRepository<ModuleEntity, UUID> {
    List<ModuleEntity> findByProgramIdOrderByOrderIndexAsc(UUID programId);
    int countByProgramId(UUID programId);
}
