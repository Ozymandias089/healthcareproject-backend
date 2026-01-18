package com.hcproj.healthcareprojectbackend.workout.repository;

import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long> {
    List<ExerciseEntity> findAllByIsActiveTrue();
}
