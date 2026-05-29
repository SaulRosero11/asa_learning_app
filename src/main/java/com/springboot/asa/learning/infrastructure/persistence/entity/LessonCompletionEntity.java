package com.springboot.asa.learning.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "lesson_completions")
public class LessonCompletionEntity {

    @EmbeddedId
    private LessonCompletionId id;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false, updatable = false)
    private OffsetDateTime completedAt;
}
