package com.hcproj.healthcareprojectbackend.calendar.dto.internal;

import com.hcproj.healthcareprojectbackend.calendar.entity.CalendarStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DaysStatusDTO(
        LocalDate date,
        CalendarStatus status
) {}
