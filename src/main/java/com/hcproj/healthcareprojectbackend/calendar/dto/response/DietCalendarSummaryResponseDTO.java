package com.hcproj.healthcareprojectbackend.calendar.dto.response;

import com.hcproj.healthcareprojectbackend.calendar.dto.internal.DaysStatusDTO;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record DietCalendarSummaryResponseDTO(
        LocalDate startDate,
        LocalDate endDate,
        List<DaysStatusDTO> days
) {}
