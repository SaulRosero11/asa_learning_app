package com.springboot.asa.learning.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonCompletionId implements Serializable {

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "user_id")
    private UUID userId;
}
