package com.hcproj.healthcareprojectbackend.calendar.dto.internal;

import lombok.Builder;

@Builder
public record MemoDTO(
        String memoId,
        String date,
        String content
) {}
