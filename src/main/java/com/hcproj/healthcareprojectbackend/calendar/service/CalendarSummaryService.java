package com.hcproj.healthcareprojectbackend.calendar.service;

import com.hcproj.healthcareprojectbackend.calendar.dto.response.DailyDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.CalendarRangeResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.CalendarRangeResponseDTO.*;
import com.hcproj.healthcareprojectbackend.calendar.repository.CalendarDayNoteRepository;
import com.hcproj.healthcareprojectbackend.diet.entity.DietDayEntity;
import com.hcproj.healthcareprojectbackend.diet.entity.DietMealEntity;
import com.hcproj.healthcareprojectbackend.diet.entity.DietMealItemEntity;
import com.hcproj.healthcareprojectbackend.diet.entity.FoodEntity;
import com.hcproj.healthcareprojectbackend.diet.repository.DietDayRepository;
import com.hcproj.healthcareprojectbackend.diet.repository.DietMealItemRepository;
import com.hcproj.healthcareprojectbackend.diet.repository.DietMealRepository;
import com.hcproj.healthcareprojectbackend.diet.repository.FoodRepository;
import com.hcproj.healthcareprojectbackend.pt.entity.PtReservationStatus;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutDayEntity;
import com.hcproj.healthcareprojectbackend.workout.entity.WorkoutItemEntity;
import com.hcproj.healthcareprojectbackend.workout.repository.ExerciseRepository;
import com.hcproj.healthcareprojectbackend.workout.repository.WorkoutDayRepository;
import com.hcproj.healthcareprojectbackend.workout.repository.WorkoutItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarSummaryService {

    private final WorkoutDayRepository workoutDayRepository;
    private final WorkoutItemRepository workoutItemRepository;
    private final ExerciseRepository exerciseRepository;

    private final DietDayRepository dietDayRepository;
    private final DietMealRepository dietMealRepository;
    private final DietMealItemRepository dietMealItemRepository;
    private final FoodRepository foodRepository;

    private final PtRoomRepository ptRoomRepository;

    private final CalendarDayNoteRepository calendarDayNoteRepository;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    /**
     * ✅ 주간/월간 범용 캘린더 상태 조회
     */
    public CalendarRangeResponseDTO getWeeklyCalendar(Long userId, LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);

        Map<LocalDate, WorkoutStatus> workoutMap = getWorkoutStatusMapOptimized(userId, startDate, endDate);
        Map<LocalDate, DietStatus> dietMap = getDietStatusMap(userId, startDate, endDate);
        Map<LocalDate, VideoPtStatus> ptMap = getVideoPtStatusMap(userId, startDate, endDate);
        Map<LocalDate, Boolean> memoMap = getMemoExistenceMap(userId, startDate, endDate);

        List<DayStatusDTO> days = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            days.add(DayStatusDTO.builder()
                    .date(date.toString())
                    .workout(WorkoutStatusDTO.builder()
                            .status(workoutMap.getOrDefault(date, WorkoutStatus.NONE))
                            .build())
                    .diet(DietStatusDTO.builder()
                            .status(dietMap.getOrDefault(date, DietStatus.NONE))
                            .build())
                    .videoPt(VideoPtStatusDTO.builder()
                            .status(ptMap.getOrDefault(date, VideoPtStatus.NONE))
                            .build())
                    .memo(MemoExistenceDTO.builder()
                            .exists(memoMap.getOrDefault(date, false))
                            .build())
                    .build());
        }

        return CalendarRangeResponseDTO.builder()
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .days(days)
                .build();
    }

    /**
     * 일일 상세 요약 조회
     */
    public DailyDetailResponseDTO getDailyDetail(Long userId, LocalDate date) {
        return DailyDetailResponseDTO.builder()
                .date(date.toString())
                .workout(getWorkoutSummary(userId, date))
                .diet(getDietSummary(userId, date))
                .videoPt(getVideoPtSummary(userId, date))
                .memo(getMemoSummary(userId, date))
                .build();
    }

    // ==================== Range Validation ====================

    /**
     * 범용(주/월)이라도 무제한 범위는 위험.
     * UI 정책에 맞춰 MAX_RANGE_DAYS 조절해.
     */
    private static final int MAX_RANGE_DAYS = 35;

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate/endDate must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be <= endDate");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("date range too large: " + days + " days");
        }
    }

    // ==================== Memo (Already optimal) ====================

    private Map<LocalDate, Boolean> getMemoExistenceMap(Long userId, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = calendarDayNoteRepository.findNoteDatesInRange(userId, startDate, endDate);
        if (dates.isEmpty()) return Collections.emptyMap();

        Map<LocalDate, Boolean> map = new HashMap<>();
        for (LocalDate d : dates) map.put(d, true);
        return map;
    }

    // ==================== Workout (Optimized: remove N+1) ====================

    private Map<LocalDate, WorkoutStatus> getWorkoutStatusMapOptimized(Long userId, LocalDate startDate, LocalDate endDate) {
        List<WorkoutDayEntity> days = workoutDayRepository.findAllByUserIdAndLogDateBetween(userId, startDate, endDate);
        if (days.isEmpty()) return Collections.emptyMap();

        Map<Long, LocalDate> dayIdToDate = days.stream()
                .collect(Collectors.toMap(WorkoutDayEntity::getWorkoutDayId, WorkoutDayEntity::getLogDate));

        List<Long> dayIds = new ArrayList<>(dayIdToDate.keySet());
        List<WorkoutItemEntity> items = workoutItemRepository.findAllByWorkoutDayIdIn(dayIds);
        if (items.isEmpty()) return Collections.emptyMap();

        // workoutDayId별 total/checked 집계
        Map<Long, long[]> agg = new HashMap<>(); // [0]=total, [1]=checked
        for (WorkoutItemEntity item : items) {
            Long dayId = item.getWorkoutDayId();
            if (dayId == null) continue;

            long[] a = agg.computeIfAbsent(dayId, k -> new long[2]);
            a[0] += 1;
            if (Boolean.TRUE.equals(item.getIsChecked())) a[1] += 1;
        }

        Map<LocalDate, WorkoutStatus> result = new HashMap<>();
        for (Map.Entry<Long, long[]> e : agg.entrySet()) {
            LocalDate date = dayIdToDate.get(e.getKey());
            if (date == null) continue;

            long total = e.getValue()[0];
            long checked = e.getValue()[1];

            if (total == 0) continue;
            if (checked == 0) result.put(date, WorkoutStatus.FAILED);
            else if (checked == total) result.put(date, WorkoutStatus.COMPLETE);
            else result.put(date, WorkoutStatus.INCOMPLETE);
        }

        return result;
    }

    // ==================== Diet ====================

    private Map<LocalDate, DietStatus> getDietStatusMap(Long userId, LocalDate startDate, LocalDate endDate) {
        List<DietDayEntity> days = dietDayRepository.findAllByUserIdAndLogDateBetween(userId, startDate, endDate);
        if (days.isEmpty()) return Collections.emptyMap();

        Map<Long, LocalDate> dayIdToDate = days.stream()
                .collect(Collectors.toMap(DietDayEntity::getDietDayId, DietDayEntity::getLogDate));

        List<Long> dietDayIds = new ArrayList<>(dayIdToDate.keySet());

        // dayId IN (...) 으로 meals 한 번에
        List<DietMealEntity> meals = dietMealRepository.findAllByDietDayIdInOrderByDietDayIdAscSortOrderAsc(dietDayIds);
        if (meals.isEmpty()) return Collections.emptyMap();

        List<Long> mealIds = meals.stream().map(DietMealEntity::getDietMealId).toList();

        // mealId IN (...) 으로 items 한 번에
        List<DietMealItemEntity> items = dietMealItemRepository.findAllByDietMealIdIn(mealIds);
        if (items.isEmpty()) return Collections.emptyMap();

        // mealId -> dietDayId
        Map<Long, Long> mealIdToDietDayId = meals.stream()
                .collect(Collectors.toMap(DietMealEntity::getDietMealId, DietMealEntity::getDietDayId));

        // dietDayId별 total/checked 집계
        Map<Long, long[]> agg = new HashMap<>(); // [0]=total, [1]=checked
        for (DietMealItemEntity item : items) {
            Long dietDayId = mealIdToDietDayId.get(item.getDietMealId());
            if (dietDayId == null) continue;

            long[] a = agg.computeIfAbsent(dietDayId, k -> new long[2]);
            a[0] += 1;
            if (Boolean.TRUE.equals(item.getIsChecked())) a[1] += 1;
        }

        Map<LocalDate, DietStatus> result = new HashMap<>();
        for (Map.Entry<Long, long[]> e : agg.entrySet()) {
            LocalDate date = dayIdToDate.get(e.getKey());
            if (date == null) continue;

            long total = e.getValue()[0];
            long checked = e.getValue()[1];

            if (total == 0) continue;
            if (checked == 0) result.put(date, DietStatus.FAILED);
            else if (checked == total) result.put(date, DietStatus.COMPLETE);
            else result.put(date, DietStatus.INCOMPLETE);
        }

        return result;
    }

    // ==================== Video PT ====================

    private Map<LocalDate, VideoPtStatus> getVideoPtStatusMap(Long userId, LocalDate startDate, LocalDate endDate) {
        // LocalDate 범위를 Instant 범위로 변환: [start 00:00, end+1 00:00)
        Instant startInclusive = startDate.atStartOfDay(ZONE_ID).toInstant();
        Instant endExclusive = endDate.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

        List<Instant> startAts = ptRoomRepository.findReservedStartAtsInRange(
                userId,
                PtReservationStatus.REQUESTED,
                startInclusive,
                endExclusive
        );

        if (startAts.isEmpty()) return Collections.emptyMap();

        Map<LocalDate, VideoPtStatus> map = new HashMap<>();
        for (Instant startAt : startAts) {
            if (startAt == null) continue;
            LocalDate date = startAt.atZone(ZONE_ID).toLocalDate();
            map.put(date, VideoPtStatus.HAS_RESERVATION);
        }
        return map;
    }

    // ==================== Daily Detail (keep as-is for now) ====================

    private DailyDetailResponseDTO.WorkoutSummaryDTO getWorkoutSummary(Long userId, LocalDate date) {
        return workoutDayRepository.findByUserIdAndLogDate(userId, date)
                .map(day -> {
                    List<WorkoutItemEntity> items = workoutItemRepository.findAllByWorkoutDayIdOrderBySortOrderAsc(day.getWorkoutDayId());
                    if (items.isEmpty()) {
                        return DailyDetailResponseDTO.WorkoutSummaryDTO.builder().exists(false).build();
                    }

                    int totalMin = items.stream().mapToInt(i -> i.getDurationMinutes() != null ? i.getDurationMinutes() : 0).sum();
                    List<String> names = exerciseRepository.findAllById(
                            items.stream().limit(2).map(WorkoutItemEntity::getExerciseId).toList()
                    ).stream().map(ExerciseEntity::getName).toList();

                    String summary = (day.getTitle() != null ? day.getTitle() + " · " : "") + totalMin + "분";

                    return DailyDetailResponseDTO.WorkoutSummaryDTO.builder()
                            .exists(true)
                            .summary(summary)
                            .itemsPreview(names)
                            .build();
                })
                .orElse(DailyDetailResponseDTO.WorkoutSummaryDTO.builder().exists(false).build());
    }

    private DailyDetailResponseDTO.DietSummaryDTO getDietSummary(Long userId, LocalDate date) {
        return dietDayRepository.findByUserIdAndLogDate(userId, date)
                .map(day -> {
                    List<DietMealEntity> meals = dietMealRepository.findAllByDietDayIdOrderBySortOrderAsc(day.getDietDayId());
                    if (meals.isEmpty()) {
                        return DailyDetailResponseDTO.DietSummaryDTO.builder().exists(false).build();
                    }

                    List<Long> mealIds = meals.stream().map(DietMealEntity::getDietMealId).toList();
                    List<DietMealItemEntity> items = dietMealItemRepository.findAllByDietMealIdIn(mealIds);

                    int totalCal = 0;
                    if (!items.isEmpty()) {
                        Map<Long, FoodEntity> foodMap = foodRepository.findAllById(
                                items.stream().map(DietMealItemEntity::getFoodId).distinct().toList()
                        ).stream().collect(Collectors.toMap(FoodEntity::getFoodId, f -> f));

                        for (DietMealItemEntity item : items) {
                            FoodEntity food = foodMap.get(item.getFoodId());
                            if (food != null) totalCal += food.getCalories() * item.getCount();
                        }
                    }

                    return DailyDetailResponseDTO.DietSummaryDTO.builder()
                            .exists(true)
                            .summary(meals.size() + "끼 · " + totalCal + "kcal")
                            .build();
                })
                .orElse(DailyDetailResponseDTO.DietSummaryDTO.builder().exists(false).build());
    }

    private DailyDetailResponseDTO.VideoPtSummaryDTO getVideoPtSummary(Long userId, LocalDate date) {
        Instant startInclusive = date.atStartOfDay(ZONE_ID).toInstant();
        Instant endExclusive = date.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

        var rows = ptRoomRepository.findDailyVideoPtRows(
                userId,
                PtReservationStatus.REQUESTED,
                startInclusive,
                endExclusive
        );

        if (rows.isEmpty()) {
            return DailyDetailResponseDTO.VideoPtSummaryDTO.builder()
                    .exists(false)
                    .build();
        }

        // 하루에 예약이 여러 개일 수 있음 → 정책 선택
        // 1) 첫 예약만 표시
        var first = rows.getFirst();
        String time = first.scheduledStartAt()
                .atZone(ZONE_ID)
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        String summary = first.trainerNickname() + " · " + time;

        // 2) 여러 개면 "+n" 표시하고 싶으면(선택)
        if (rows.size() > 1) {
            summary += " 외 " + (rows.size() - 1) + "건";
        }

        return DailyDetailResponseDTO.VideoPtSummaryDTO.builder()
                .exists(true)
                .summary(summary)
                .build();
    }


    private DailyDetailResponseDTO.MemoSummaryDTO getMemoSummary(Long userId, LocalDate date) {
        return calendarDayNoteRepository.findByUserIdAndNoteDate(userId, date)
                .map(note -> DailyDetailResponseDTO.MemoSummaryDTO.builder()
                        .memoId(note.getCalendarDayNoteId())
                        .content(note.getNote())
                        .build())
                .orElse(null);
    }
}
