package com.springboot.asa.learning.infrastructure.persistence.repository;

import com.springboot.asa.learning.infrastructure.persistence.entity.OnboardingQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOnboardingQuestionRepository extends JpaRepository<OnboardingQuestionEntity, UUID> {
    List<OnboardingQuestionEntity> findByActiveTrueOrderByDisplayOrderAsc();
    List<OnboardingQuestionEntity> findAllByOrderByDisplayOrderAsc();
    Optional<OnboardingQuestionEntity> findByFieldKey(String fieldKey);

    @Query(value = "SELECT MAX(display_order) FROM onboarding_questions", nativeQuery = true)
    Integer findMaxDisplayOrder();

    @Query(value = "SELECT COUNT(*) FROM user_profiles WHERE jsonb_exists(demographic_data, :fieldKey)", nativeQuery = true)
    long countProfilesWithFieldKey(@Param("fieldKey") String fieldKey);
}
