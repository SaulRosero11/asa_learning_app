package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.NewsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaNewsRepository extends JpaRepository<NewsEntity, UUID> {
    Page<NewsEntity> findByProgramIdAndDeletedAtIsNullOrderByPublishedAtDesc(UUID programId, Pageable pageable);
}
