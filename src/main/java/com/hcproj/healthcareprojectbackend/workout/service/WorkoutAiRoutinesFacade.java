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
        deleteExistingInWindow(userId, startDate, endDate);

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

    private void deleteExistingInWindow(Long userId, LocalDate startDate, LocalDate endDate) {
        List<WorkoutDayEntity> existing = workoutDayRepository
                .findByUserIdAndLogDateBetween(userId, startDate, endDate);

        if (existing.isEmpty()) return;

        List<Long> dayIds = existing.stream()
                .map(WorkoutDayEntity::getWorkoutDayId)
                .toList();

        // 1) items 삭제
        workoutItemRepository.deleteByWorkoutDayIdIn(dayIds);

        // 2) days도 삭제 (권장: "목금월화만 남는다"를 보장)
        workoutDayRepository.deleteAllByIdInBatch(dayIds);
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
        // 0) AI days를 날짜 기준으로 정렬(안 해도 되지만 안정성↑)
        List<WorkoutAiRoutineResult.Day> aiDays = ai.days().stream()
                .sorted(Comparator.comparing(WorkoutAiRoutineResult.Day::logDate))
                .toList();

        List<LocalDate> dates = aiDays.stream()
                .map(WorkoutAiRoutineResult.Day::logDate)
                .toList();

        // 1) 기존 day 조회
        Map<LocalDate, WorkoutDayEntity> existing =
                workoutDayRepository.findByUserIdAndLogDateIn(userId, dates).stream()
                        .collect(Collectors.toMap(WorkoutDayEntity::getLogDate, d -> d));

        // 2) day 업서트(있으면 title 갱신, 없으면 생성)
        List<WorkoutDayEntity> daysToSave = new ArrayList<>();
        for (var d : aiDays) {
            WorkoutDayEntity day = existing.get(d.logDate());
            if (day == null) {
                day = WorkoutDayEntity.builder()
                        .userId(userId)
                        .logDate(d.logDate())
                        .totalMinutes(d.totalMinutes())
                        .title(d.title())
                        .build();
            } else {
                day.replaceTitle(d.title()); // 엔티티에 메서드 필요
            }
            daysToSave.add(day);
        }

        List<WorkoutDayEntity> savedDays = workoutDayRepository.saveAll(daysToSave);

        // 3) 날짜 -> dayId 매핑
        Map<LocalDate, Long> dateToDayId = savedDays.stream()
                .collect(Collectors.toMap(WorkoutDayEntity::getLogDate, WorkoutDayEntity::getWorkoutDayId));

        // 5) item 재삽입
        List<WorkoutItemEntity> itemEntities = new ArrayList<>();
        for (var day : aiDays) {
            Long dayId = dateToDayId.get(day.logDate());
            if (dayId == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }

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
                        .isChecked(false) // 정책: 새로 생성된 루틴은 미체크
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
