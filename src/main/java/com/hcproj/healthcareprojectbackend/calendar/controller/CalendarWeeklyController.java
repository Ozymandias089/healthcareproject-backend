package com.hcproj.healthcareprojectbackend.calendar.controller;

import com.hcproj.healthcareprojectbackend.calendar.dto.response.DietCalendarSummaryResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.WorkoutCalendarSummaryResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.service.CalendarWeeklyService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CalendarWeeklyController {

    private final CalendarWeeklyService calendarWeeklyService;

    @GetMapping(path = "/workouts/calendar/summary")
    public ApiResponse<WorkoutCalendarSummaryResponseDTO> getWorkoutSummary(
            @CurrentUserId Long id,
            @RequestParam() @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam() @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){
        WorkoutCalendarSummaryResponseDTO responseDTO = calendarWeeklyService.getWorkout(id, startDate, endDate);
        return ApiResponse.ok(responseDTO);
    }

    @GetMapping(path = "/diets/calendar/summary")
    public ApiResponse<DietCalendarSummaryResponseDTO> getDietSummary(
            @CurrentUserId Long id,
            @RequestParam() @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam() @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){
        DietCalendarSummaryResponseDTO responseDTO = calendarWeeklyService.getDiet(id, startDate, endDate);
        return ApiResponse.ok(responseDTO);
    }
}
