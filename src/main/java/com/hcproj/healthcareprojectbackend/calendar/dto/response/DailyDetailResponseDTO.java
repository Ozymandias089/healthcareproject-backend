package com.hcproj.healthcareprojectbackend.calendar.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DailyDetailResponseDTO(
        String date,
        WorkoutSummaryDTO workout,
        DietSummaryDTO diet,
        VideoPtSummaryDTO videoPt,
        MemoSummaryDTO memo
) {
    @Builder
    public record WorkoutSummaryDTO(
            boolean exists,
            String summary,
            List<String> itemsPreview
    ) {}

    @Builder
    public record DietSummaryDTO(
            boolean exists,
            String summary
    ) {}

    @Builder
    public record VideoPtSummaryDTO(
            boolean exists,
            String summary
    ) {}

    @Builder
    public record MemoSummaryDTO(
            Long memoId,
            String content
    ) {}
}