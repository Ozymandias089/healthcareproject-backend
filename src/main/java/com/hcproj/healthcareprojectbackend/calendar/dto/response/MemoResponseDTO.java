package com.hcproj.healthcareprojectbackend.calendar.dto.response;

import com.hcproj.healthcareprojectbackend.calendar.dto.internal.MemoDTO;
import lombok.Builder;

@Builder
public record MemoResponseDTO(
        String message,
        MemoDTO memo
) {}
