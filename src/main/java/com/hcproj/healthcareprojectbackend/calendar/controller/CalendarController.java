package com.hcproj.healthcareprojectbackend.calendar.controller;

import com.hcproj.healthcareprojectbackend.calendar.dto.request.PutMemoRequestDTO;
import com.hcproj.healthcareprojectbackend.calendar.dto.response.MemoResponseDTO;
import com.hcproj.healthcareprojectbackend.calendar.service.CalendarService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/memos")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @PutMapping("/{date}")
    public ApiResponse<MemoResponseDTO> putMemo(
            @CurrentUserId Long userId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @Valid @RequestBody PutMemoRequestDTO requestDTO
    ) {
        MemoResponseDTO responseDTO =
                calendarService.putMemo(userId, date, requestDTO.content());
        return ApiResponse.ok(responseDTO);
    }
    @GetMapping("/{date}")
    public ApiResponse<MemoResponseDTO> getMemo(
            @CurrentUserId Long userId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        MemoResponseDTO responseDTO =
                calendarService.getMemo(userId, date);
        return ApiResponse.ok(responseDTO);
    }
}
