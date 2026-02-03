package com.hcproj.healthcareprojectbackend.calendar.dto.internal;

import java.time.LocalDate;

public record DayCountRow(
        LocalDate date,
        long plannedCount,
        long doneCount
) {}
