package com.hcproj.healthcareprojectbackend.workout.repository;

import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.List;

/**
 * 사용자 일자별 운동 기록(WorkoutDay) Repository.
 *
 * <p>
 * 하루 단위 운동 기록의 존재 여부 확인 및
 * 캘린더/주간 조회에 사용된다.
 * </p>
 */
public interface WorkoutDayRepository extends JpaRepository<WorkoutDayEntity, Long> {
    /**
     * 특정 날짜의 운동 기록 조회.
     */
    Optional<WorkoutDayEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    /**
     * 특정 기간의 운동 기록 조회 (주간 캘린더용).
     */
    List<WorkoutDayEntity> findAllByUserIdAndLogDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    /**
     * 특정 날짜 목록에 해당하는 운동 기록 조회.
     */
    List<WorkoutDayEntity> findByUserIdAndLogDateIn(Long userId, Collection<LocalDate> dates);

    interface DayCountView {
        LocalDate getDate();
        Long getPlannedCount();
        Long getDoneCount();
    }

    @Query(value = """
    SELECT
        wd.log_date AS date,
        COUNT(wi.workout_item_id) AS plannedCount,
        SUM(CASE WHEN wi.is_checked = TRUE THEN 1 ELSE 0 END) AS doneCount
    FROM workout_days wd
    JOIN workout_items wi
      ON wi.workout_day_id = wd.workout_day_id
    WHERE wd.user_id = :userId
      AND wd.log_date BETWEEN :startDate AND :endDate
    GROUP BY wd.log_date
    """, nativeQuery = true)
    List<DayCountView> findWorkoutItemCountsGroupedByDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<WorkoutDayEntity> findByUserIdAndLogDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
