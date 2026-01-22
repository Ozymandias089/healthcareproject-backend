package com.hcproj.healthcareprojectbackend.calendar.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.DailyDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.WeeklyCalendarResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.WeeklyCalendarResponseDTO.*;
import com.hcproj.healthcareprojectbackend.calendar.entity.CalendarDayNoteEntity;
import com.hcproj.healthcareprojectbackend.calendar.repository.CalendarDayNoteRepository;
import com.hcproj.healthcareprojectbackend.diet.entity.DietDayEntity;
import com.hcproj.healthcareprojectbackend.diet.entity.DietMealEntity;
import com.hcproj.healthcareprojectbackend.diet.entity.DietMealItemEntity;
import com.hcproj.healthcareprojectbackend.diet.entity.FoodEntity;
import com.hcproj.healthcareprojectbackend.diet.repository.DietDayRepository;
import com.hcproj.healthcareprojectbackend.diet.repository.DietMealItemRepository;
import com.hcproj.healthcareprojectbackend.diet.repository.DietMealRepository;
import com.hcproj.healthcareprojectbackend.diet.repository.FoodRepository;
import com.hcproj.healthcareprojectbackend.pt.entity.PtReservationEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtReservationStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.repository.PtReservationRepository;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private final PtReservationRepository ptReservationRepository;
    private final PtRoomRepository ptRoomRepository;
    private final UserRepository userRepository;
    private final CalendarDayNoteRepository calendarDayNoteRepository;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    /**
     * 주간 캘린더 상태 조회
     */
    public WeeklyCalendarResponseDTO getWeeklyCalendar(Long userId, LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);

        Map<LocalDate, WorkoutStatus> workoutMap = getWorkoutStatusMap(userId, startDate, endDate);
        Map<LocalDate, DietStatus> dietMap = getDietStatusMap(userId, startDate, endDate);
        Map<LocalDate, VideoPtStatus> ptMap = getVideoPtStatusMap(userId, startDate, endDate);

        List<DayStatusDTO> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
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
                    .build());
        }

        return WeeklyCalendarResponseDTO.builder()
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

    // ==================== Private 메서드 ====================

    private Map<LocalDate, WorkoutStatus> getWorkoutStatusMap(Long userId, LocalDate startDate, LocalDate endDate) {
        List<WorkoutDayEntity> workoutDays = workoutDayRepository.findAllByUserIdAndLogDateBetween(userId, startDate, endDate);
        Map<LocalDate, WorkoutStatus> statusMap = new HashMap<>();

        for (WorkoutDayEntity day : workoutDays) {
            List<WorkoutItemEntity> items = workoutItemRepository.findAllByWorkoutDayIdOrderBySortOrderAsc(day.getWorkoutDayId());
            if (items.isEmpty()) continue;

            long checked = items.stream().filter(WorkoutItemEntity::getIsChecked).count();
            if (checked == 0) statusMap.put(day.getLogDate(), WorkoutStatus.FAILED);
            else if (checked == items.size()) statusMap.put(day.getLogDate(), WorkoutStatus.COMPLETE);
            else statusMap.put(day.getLogDate(), WorkoutStatus.INCOMPLETE);
        }
        return statusMap;
    }

    private Map<LocalDate, DietStatus> getDietStatusMap(Long userId, LocalDate startDate, LocalDate endDate) {
        List<DietDayEntity> dietDays = dietDayRepository.findAllByUserIdAndLogDateBetween(userId, startDate, endDate);
        Map<LocalDate, DietStatus> statusMap = new HashMap<>();

        for (DietDayEntity day : dietDays) {
            List<DietMealEntity> meals = dietMealRepository.findAllByDietDayIdOrderBySortOrderAsc(day.getDietDayId());
            if (meals.isEmpty()) continue;

            List<Long> mealIds = meals.stream().map(DietMealEntity::getDietMealId).toList();
            List<DietMealItemEntity> items = dietMealItemRepository.findAllByDietMealIdIn(mealIds);
            if (items.isEmpty()) continue;

            long checked = items.stream().filter(DietMealItemEntity::getIsChecked).count();
            if (checked == 0) statusMap.put(day.getLogDate(), DietStatus.FAILED);
            else if (checked == items.size()) statusMap.put(day.getLogDate(), DietStatus.COMPLETE);
            else statusMap.put(day.getLogDate(), DietStatus.INCOMPLETE);
        }
        return statusMap;
    }

    private Map<LocalDate, VideoPtStatus> getVideoPtStatusMap(Long userId, LocalDate startDate, LocalDate endDate) {
        List<PtReservationEntity> reservations = ptReservationRepository.findAllByUserIdAndStatus(userId, PtReservationStatus.REQUESTED);
        if (reservations.isEmpty()) return Collections.emptyMap();

        List<Long> roomIds = reservations.stream().map(PtReservationEntity::getPtRoomId).toList();
        List<PtRoomEntity> rooms = ptRoomRepository.findAllByPtRoomIdIn(roomIds);

        Map<LocalDate, VideoPtStatus> statusMap = new HashMap<>();
        for (PtRoomEntity room : rooms) {
            if (room.getScheduledStartAt() == null) continue;
            LocalDate roomDate = room.getScheduledStartAt().atZone(ZONE_ID).toLocalDate();
            if (!roomDate.isBefore(startDate) && !roomDate.isAfter(endDate)) {
                statusMap.put(roomDate, VideoPtStatus.HAS_RESERVATION);
            }
        }
        return statusMap;
    }

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
        List<PtReservationEntity> reservations = ptReservationRepository.findAllByUserIdAndStatus(userId, PtReservationStatus.REQUESTED);
        if (reservations.isEmpty()) {
            return DailyDetailResponseDTO.VideoPtSummaryDTO.builder().exists(false).build();
        }

        List<Long> roomIds = reservations.stream().map(PtReservationEntity::getPtRoomId).toList();
        List<PtRoomEntity> rooms = ptRoomRepository.findAllByPtRoomIdIn(roomIds);

        for (PtRoomEntity room : rooms) {
            if (room.getScheduledStartAt() == null) continue;
            LocalDate roomDate = room.getScheduledStartAt().atZone(ZONE_ID).toLocalDate();
            if (roomDate.equals(date)) {
                String trainerName = userRepository.findById(room.getTrainerId())
                        .map(UserEntity::getNickname).orElse("트레이너");
                String time = room.getScheduledStartAt().atZone(ZONE_ID).toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm"));

                return DailyDetailResponseDTO.VideoPtSummaryDTO.builder()
                        .exists(true)
                        .summary(trainerName + " · " + time)
                        .build();
            }
        }
        return DailyDetailResponseDTO.VideoPtSummaryDTO.builder().exists(false).build();
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