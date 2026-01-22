package com.hcproj.healthcareprojectbackend.workout.repository;

import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutItemRepository extends JpaRepository<WorkoutItemEntity, Long> {
    List<WorkoutItemEntity> findAllByWorkoutDayIdOrderBySortOrderAsc(Long workoutDayId);
    List<WorkoutItemEntity> findAllByWorkoutDayIdIn(List<Long> workoutDayIds);
}
