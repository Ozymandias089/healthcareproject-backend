package com.hcproj.healthcareprojectbackend.workout.service;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.workout.dto.response.WorkoutDayResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.WorkoutItemCheckResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutDayEntity;
import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutItemEntity;
import com.hcproj.healthcareprojectbackend.workout.repository.ExerciseRepository;
import com.hcproj.healthcareprojectbackend.workout.repository.WorkoutDayRepository;
import com.hcproj.healthcareprojectbackend.workout.repository.WorkoutItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 운동 계획 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutDayService {

    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutItemRepository workoutItemRepository;
    private final ExerciseRepository exerciseRepository;

    /**
     * 특정 날짜의 운동 계획 조회
     *
     * @param userId 사용자 ID
     * @param date   조회할 날짜
     * @return 운동 계획 응답 DTO
     */
    public WorkoutDayResponseDTO getWorkoutDayByDate(Long userId, LocalDate date) {
        // 1. workout_days 조회 (user_id + date)
        WorkoutDayEntity workoutDay = workoutDayRepository.findByUserIdAndLogDate(userId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKOUT_DAY_NOT_FOUND));

        // 2. workout_items 조회 (sort_order ASC)
        List<WorkoutItemEntity> workoutItems = workoutItemRepository
                .findAllByWorkoutDayIdOrderBySortOrderAsc(workoutDay.getWorkoutDayId());

        // 3. exercise_id 목록 추출 → Exercise 조회 (N+1 방지)
        List<Long> exerciseIds = workoutItems.stream()
                .map(WorkoutItemEntity::getExerciseId)
                .distinct()
                .toList();

        Map<Long, ExerciseEntity> exerciseMap = exerciseRepository.findAllById(exerciseIds)
                .stream()
                .collect(Collectors.toMap(ExerciseEntity::getExerciseId, e -> e));

        // 5. 완료 개수 계산
        int completedCount = (int) workoutItems.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsChecked()))
                .count();

        // 6. items 변환
        List<WorkoutDayResponseDTO.WorkoutItemDTO> itemDTOs = workoutItems.stream()
                .map(item -> {
                    ExerciseEntity exercise = exerciseMap.get(item.getExerciseId());
                    String exerciseName = (exercise != null) ? exercise.getName() : "알 수 없는 운동";

                    return WorkoutDayResponseDTO.WorkoutItemDTO.builder()
                            .workoutItemId(item.getWorkoutItemId())
                            .exerciseId(item.getExerciseId())
                            .name(exerciseName)
                            .quantity(item.getReps())
                            .sets(item.getSets())
                            .restSeconds(item.getRestSecond())
                            .isChecked(item.getIsChecked())
                            .sortOrder(item.getSortOrder())
                            .build();
                })
                .toList();

        // 7. 응답 DTO 생성
        return WorkoutDayResponseDTO.builder()
                .date(date.toString())
                .workoutDayId(workoutDay.getWorkoutDayId())
                .title(workoutDay.getTitle())
                .totalMinutes(workoutDay.getTotalMinutes())
                .exerciseCount(workoutItems.size())
                .completedCount(completedCount)
                .items(itemDTOs)
                .build();
    }

    /**
     * 운동 항목 체크 상태 업데이트
     *
     * @param userId        사용자 ID
     * @param workoutItemId 운동 항목 ID
     * @param checked       체크 상태
     * @return 업데이트된 응답 DTO
     */
    @Transactional
    public WorkoutItemCheckResponseDTO updateWorkoutItemCheck(Long userId, Long workoutItemId, Boolean checked) {
        // 1. workout_item 조회
        WorkoutItemEntity workoutItem = workoutItemRepository.findById(workoutItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKOUT_ITEM_NOT_FOUND));

        // 2. workout_day 조회하여 소유권 확인
        WorkoutDayEntity workoutDay = workoutDayRepository.findById(workoutItem.getWorkoutDayId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKOUT_DAY_NOT_FOUND));

        // 3. 본인 소유인지 확인
        if (!workoutDay.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 4. 체크 상태 업데이트
        workoutItem.updateChecked(checked);

        // 5. 응답 반환
        return WorkoutItemCheckResponseDTO.builder()
                .workoutItemId(workoutItem.getWorkoutItemId())
                .isChecked(workoutItem.getIsChecked())
                .build();
    }

}