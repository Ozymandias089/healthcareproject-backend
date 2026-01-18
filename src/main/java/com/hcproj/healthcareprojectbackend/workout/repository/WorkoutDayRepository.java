package com.hcproj.healthcareprojectbackend.workout.repository;

import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WorkoutDayRepository extends JpaRepository<WorkoutDayEntity, Long> {
    Optional<WorkoutDayEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);
}
