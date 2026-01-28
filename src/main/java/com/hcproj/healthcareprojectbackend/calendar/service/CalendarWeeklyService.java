package com.hcproj.healthcareprojectbackend.calendar.service;

import com.hcproj.healthcareprojectbackend.calendar.dto.internal.DaysStatusDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.DietCalendarSummaryResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.WorkoutCalendarSummaryResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.entity.CalendarStatus;
import com.hcproj.healthcareprojectbackend.diet.repository.DietDayRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.workout.repository.WorkoutDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarWeeklyService {

    private final DietDayRepository dietDayRepository;
    private final WorkoutDayRepository workoutDayRepository;

    @Transactional(readOnly = true)
    public WorkoutCalendarSummaryResponseDTO getWorkout(Long id, LocalDate startDate, LocalDate endDate) {

        if (endDate.isBefore(startDate)) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        List<WorkoutDayRepository.DayCountView> rows =
                workoutDayRepository.findWorkoutItemCountsGroupedByDate(id, startDate, endDate);

        Map<LocalDate, WorkoutDayRepository.DayCountView> countMap =
                rows.stream().collect(Collectors.toMap(
                        WorkoutDayRepository.DayCountView::getDate,
                        Function.identity()
                ));

        List<DaysStatusDTO> days = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            WorkoutDayRepository.DayCountView row = countMap.get(date);

            CalendarStatus status;
            if (row == null) {
                status = CalendarStatus.NO_STATUS; // plannedCount == 0
            } else {
                status = resolveStatus(row.getPlannedCount(), row.getDoneCount());
            }

            days.add(DaysStatusDTO.builder()
                    .date(date)
                    .status(status)
                    .build());
        }

        return WorkoutCalendarSummaryResponseDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .days(days)
                .build();
        }

    @Transactional(readOnly = true)
    public DietCalendarSummaryResponseDTO getDiet(Long id, LocalDate startDate, LocalDate endDate) {

        if (endDate.isBefore(startDate)) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        List<DietDayRepository.DayCountView> rows =
                dietDayRepository.findDietItemCountsGroupedByDate(id, startDate, endDate);

        // date → counts 매핑
        Map<LocalDate, DietDayRepository.DayCountView> countMap =
                rows.stream().collect(Collectors.toMap(
                        DietDayRepository.DayCountView::getDate,
                        Function.identity()
                ));

        List<DaysStatusDTO> days = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DietDayRepository.DayCountView row = countMap.get(date);

            CalendarStatus status;
            if (row == null) {
                // plannedCount == 0
                status = CalendarStatus.NO_STATUS;
            } else {
                status = resolveStatus(row.getPlannedCount(), row.getDoneCount());
            }

            days.add(DaysStatusDTO.builder()
                    .date(date)
                    .status(status)
                    .build());
        }

        return DietCalendarSummaryResponseDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .days(days)
                .build();
    }

    private CalendarStatus resolveStatus(long plannedCount, long doneCount) {
        if (plannedCount == 0) {
            return CalendarStatus.NO_STATUS;
        }
        if (doneCount >= plannedCount) {
            return CalendarStatus.DONE;
        }
        return CalendarStatus.PLANNED;
    }

}
