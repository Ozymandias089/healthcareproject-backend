package com.hcproj.healthcareprojectbackend.workout.service;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.workout.ai.WorkoutAiExerciseCatalogService;
import com.hcproj.healthcareprojectbackend.workout.ai.WorkoutAiService;
import com.hcproj.healthcareprojectbackend.workout.ai.dto.WorkoutAiRoutineResult;
import com.hcproj.healthcareprojectbackend.workout.dto.request.AiWorkoutRoutinesPutRequestDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.AiWorkoutRoutinesResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutDayEntity;
import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutItemEntity;
import com.hcproj.healthcareprojectbackend.workout.repository.ExerciseRepository;
import com.hcproj.healthcareprojectbackend.workout.repository.WorkoutDayRepository;
import com.hcproj.healthcareprojectbackend.workout.repository.WorkoutItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutAiRoutinesFacade {

    private final WorkoutAiService workoutAiService;

    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutItemRepository workoutItemRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    public AiWorkoutRoutinesResponseDTO replaceRoutines(Long userId, AiWorkoutRoutinesPutRequestDTO req) {

        // 1) 요청 시작일 + 7일 윈도우
        LocalDate startDate = req.dates().stream().min(LocalDate::compareTo)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
        LocalDate endDate = startDate.plusDays(6);

        // 2) 윈도우 안의 날짜만 유효 (정렬 + 중복 제거)
        List<LocalDate> targetDates = req.dates().stream()
                .filter(d -> !d.isBefore(startDate) && !d.isAfter(endDate))
                .distinct()
                .sorted()
                .toList();

        if (targetDates.isEmpty()) {
            // 정책: 아무 것도 안 하면 에러가 더 낫다(클라 실수)
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 3) 기존 해당 날짜들만 삭제(Replace)
        deleteExisting(userId, targetDates);

        // 4) AI 호출 (allowedExercises whitelist 포함)
        WorkoutAiService.Generated generated = workoutAiService.generate(targetDates, req.additionalRequest());
        WorkoutAiRoutineResult ai = generated.result();

        // 5) 서버 검증 (exerciseId가 allowed 목록 안인지)
        Set<Long> allowedExerciseIds = generated.allowedPayload().allowedExercises().stream()
                .map(WorkoutAiExerciseCatalogService.AllowedExerciseDTO::id)
                .collect(Collectors.toSet());
        validateAiOutput(targetDates, ai, allowedExerciseIds);

        // 6) insert
        Persisted persisted = persist(userId, ai);

        // 7) 응답 조립 (exerciseName 조인 + dayOfWeek)
        return buildResponse(targetDates, ai, persisted);
    }

    private void deleteExisting(Long userId, List<LocalDate> targetDates) {
        List<WorkoutDayEntity> existing = workoutDayRepository.findByUserIdAndLogDateIn(userId, targetDates);
        if (existing.isEmpty()) return;

        List<Long> dayIds = existing.stream().map(WorkoutDayEntity::getWorkoutDayId).toList();
        workoutItemRepository.deleteByWorkoutDayIdIn(dayIds);
        workoutDayRepository.deleteAll(existing);
    }

    private void validateAiOutput(List<LocalDate> targetDates, WorkoutAiRoutineResult ai, Set<Long> allowedIds) {
        if (ai == null || ai.days() == null) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);

        // 날짜 set 비교: AI가 targetDates 외 날짜를 만들면 invalid
        Set<LocalDate> expected = new HashSet<>(targetDates);
        Set<LocalDate> got = ai.days().stream().map(WorkoutAiRoutineResult.Day::logDate).collect(Collectors.toSet());
        if (!got.equals(expected)) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);

        for (var day : ai.days()) {
            if (day.title() == null || day.title().isBlank()) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
            if (day.items() == null || day.items().isEmpty()) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);

            int order = 0;
            for (var item : day.items()) {
                if (item.displayOrder() == null || item.displayOrder() != order) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
                order++;

                if (item.exerciseId() == null || !allowedIds.contains(item.exerciseId()))
                    throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);

                // sets/reps/duration/distance 중 최소 하나는 있어야 의미있게
                boolean hasAny = item.sets() != null || item.reps() != null || item.durationMinutes() != null || item.distanceKm() != null;
                if (!hasAny) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
            }
        }
    }

    private Persisted persist(Long userId, WorkoutAiRoutineResult ai) {
        // day insert
        List<WorkoutDayEntity> dayEntities = ai.days().stream()
                .sorted(Comparator.comparing(WorkoutAiRoutineResult.Day::logDate))
                .map(d -> WorkoutDayEntity.builder()
                        .userId(userId)
                        .logDate(d.logDate())
                        .title(d.title())
                        .build())
                .toList();

        List<WorkoutDayEntity> savedDays = workoutDayRepository.saveAll(dayEntities);
        Map<LocalDate, Long> dateToDayId = savedDays.stream()
                .collect(Collectors.toMap(WorkoutDayEntity::getLogDate, WorkoutDayEntity::getWorkoutDayId));

        // item insert
        List<WorkoutItemEntity> itemEntities = new ArrayList<>();
        for (var day : ai.days()) {
            Long dayId = dateToDayId.get(day.logDate());
            for (var it : day.items()) {
                itemEntities.add(WorkoutItemEntity.builder()
                        .workoutDayId(dayId)
                        .exerciseId(it.exerciseId())
                        .sortOrder(it.displayOrder())
                        .sets(it.sets())
                        .reps(it.reps())
                        .restSecond(it.restSecond())
                        .durationMinutes(it.durationMinutes())
                        .distanceKm(it.distanceKm())
                        .rpe(it.rpe())
                        .amount(it.amount())
                        .isChecked(false) // 정책 A
                        .build());
            }
        }

        List<WorkoutItemEntity> savedItems = workoutItemRepository.saveAll(itemEntities);
        return new Persisted(savedDays, savedItems);
    }

    private AiWorkoutRoutinesResponseDTO buildResponse(
            List<LocalDate> targetDates,
            WorkoutAiRoutineResult ai,
            Persisted persisted
    ) {
        Instant now = Instant.now();

        // exerciseName map
        Set<Long> exerciseIds = persisted.items.stream().map(WorkoutItemEntity::getExerciseId).collect(Collectors.toSet());
        Map<Long, ExerciseEntity> exerciseMap = exerciseRepository.findByExerciseIdIn(exerciseIds).stream()
                .collect(Collectors.toMap(ExerciseEntity::getExerciseId, e -> e));

        Map<Long, List<WorkoutItemEntity>> itemsByDayId = persisted.items.stream()
                .collect(Collectors.groupingBy(WorkoutItemEntity::getWorkoutDayId));

        // ai day info를 logDate로 빠르게 찾기
        Map<LocalDate, WorkoutAiRoutineResult.Day> aiDayMap = ai.days().stream()
                .collect(Collectors.toMap(WorkoutAiRoutineResult.Day::logDate, d -> d));

        List<AiWorkoutRoutinesResponseDTO.Day> days = persisted.days.stream()
                .sorted(Comparator.comparing(WorkoutDayEntity::getLogDate))
                .map(day -> {
                    LocalDate logDate = day.getLogDate();
                    String dayOfWeek = toDow(logDate.getDayOfWeek());

                    List<WorkoutItemEntity> items = itemsByDayId.getOrDefault(day.getWorkoutDayId(), List.of())
                            .stream()
                            .sorted(Comparator.comparing(WorkoutItemEntity::getSortOrder))
                            .toList();

                    // totalMinutes: AI가 준 값 우선, 없으면 duration 합
                    Integer totalMinutes = aiDayMap.get(logDate) != null ? aiDayMap.get(logDate).totalMinutes() : null;
                    if (totalMinutes == null) {
                        totalMinutes = items.stream()
                                .map(WorkoutItemEntity::getDurationMinutes)
                                .filter(Objects::nonNull)
                                .reduce(0, Integer::sum);
                    }

                    List<AiWorkoutRoutinesResponseDTO.Item> itemDtos = items.stream()
                            .map(it -> {
                                ExerciseEntity ex = exerciseMap.get(it.getExerciseId());
                                if (ex == null) throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

                                return new AiWorkoutRoutinesResponseDTO.Item(
                                        it.getWorkoutItemId(),
                                        it.getSortOrder(),
                                        it.getExerciseId(),
                                        ex.getName(),
                                        it.getSets(),
                                        it.getReps(),
                                        it.getRestSecond(),
                                        it.getDurationMinutes(),
                                        it.getDistanceKm(),
                                        it.getRpe(),
                                        it.getAmount(),
                                        it.getIsChecked()
                                );
                            })
                            .toList();

                    return new AiWorkoutRoutinesResponseDTO.Day(
                            day.getWorkoutDayId(),
                            logDate,
                            dayOfWeek,
                            day.getTitle(),
                            totalMinutes,
                            itemDtos
                    );
                })
                .toList();

        return new AiWorkoutRoutinesResponseDTO(
                now,
                new AiWorkoutRoutinesResponseDTO.PlanSummary(7, targetDates.size()),
                ai.considerations() == null ? List.of() : ai.considerations(),
                days
        );
    }

    private String toDow(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }

    private record Persisted(List<WorkoutDayEntity> days, List<WorkoutItemEntity> items) {}
}
