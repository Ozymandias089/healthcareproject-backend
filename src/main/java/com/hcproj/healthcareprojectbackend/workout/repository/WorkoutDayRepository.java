package com.hcproj.healthcareprojectbackend.workout.repository;

import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface WorkoutDayRepository extends JpaRepository<WorkoutDayEntity, Long> {
    Optional<WorkoutDayEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    // 특정 기간의 운동 기록 조회 (주간 캘린더용)
    List<WorkoutDayEntity> findAllByUserIdAndLogDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
