package com.hcproj.healthcareprojectbackend.calendar.controller;

import com.hcproj.healthcareprojectbackend.calendar.dto.response.DailyDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.CalendarRangeResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.service.CalendarSummaryService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CalendarSummaryController {

    private final CalendarSummaryService calendarSummaryService;

    @GetMapping("/me/calendar/weekly")
    public ApiResponse<CalendarRangeResponseDTO> getWeeklyCalendar(
            @CurrentUserId Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.ok(calendarSummaryService.getWeeklyCalendar(userId, startDate, endDate));
    }

    @GetMapping("/calendar/day/{date}")
    public ApiResponse<DailyDetailResponseDTO> getDailyDetail(
            @CurrentUserId Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(calendarSummaryService.getDailyDetail(userId, date));
    }
}