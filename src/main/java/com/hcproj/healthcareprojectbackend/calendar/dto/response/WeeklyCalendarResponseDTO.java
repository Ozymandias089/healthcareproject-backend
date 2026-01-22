package com.hcproj.healthcareprojectbackend.calendar.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record WeeklyCalendarResponseDTO(
        String startDate,
        String endDate,
        List<DayStatusDTO> days
) {

    @Builder
    public record DayStatusDTO(
            String date,
            DietStatusDTO diet,
            VideoPtStatusDTO videoPt,
            WorkoutStatusDTO workout
    ) {}

    @Builder
    public record DietStatusDTO(DietStatus status) {}

    @Builder
    public record VideoPtStatusDTO(VideoPtStatus status) {}

    @Builder
    public record WorkoutStatusDTO(WorkoutStatus status) {}

    public enum DietStatus {
        NONE,       // 기록 없음
        COMPLETE,   // 할당량 충족
        INCOMPLETE, // 할당량 미달
        FAILED      // 진행률 0%
    }

    public enum VideoPtStatus {
        NONE,           // 예약 없음
        HAS_RESERVATION // 예약 존재
    }

    public enum WorkoutStatus {
        NONE,       // 계획 없음
        COMPLETE,   // 모든 운동 완료
        INCOMPLETE, // 일부 완료
        FAILED      // 진행률 0%
    }
}