package com.hcproj.healthcareprojectbackend.workout.repository;

import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 운동 세부 항목(WorkoutItem) Repository.
 *
 * <p>
 * 하루 운동 기록에 속한 개별 운동 항목을 관리한다.
 * </p>
 */
public interface WorkoutItemRepository extends JpaRepository<WorkoutItemEntity, Long> {
    /**
     * 하루 운동 기록에 속한 운동 항목 목록 조회 (정렬 포함).
     *
     * @param workoutDayId 운동 기록 ID
     * @return 운동 항목 목록
     */
    List<WorkoutItemEntity> findAllByWorkoutDayIdOrderBySortOrderAsc(Long workoutDayId);

    /**
     * 여러 운동 기록 ID에 속한 항목 일괄 조회.
     */
    List<WorkoutItemEntity> findAllByWorkoutDayIdIn(List<Long> workoutDayIds);

    /**
     * 여러 운동 기록 ID에 속한 항목 일괄 삭제.
     */
    void deleteByWorkoutDayIdIn(Collection<Long> workoutDayIds);
}
