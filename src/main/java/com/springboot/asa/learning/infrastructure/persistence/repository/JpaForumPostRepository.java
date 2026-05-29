package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.ForumPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaForumPostRepository extends JpaRepository<ForumPostEntity, UUID> {
    List<ForumPostEntity> findByForumIdAndParentPostIdIsNullOrderByCreatedAtAsc(UUID forumId);
    List<ForumPostEntity> findByParentPostIdOrderByCreatedAtAsc(UUID parentPostId);
}
