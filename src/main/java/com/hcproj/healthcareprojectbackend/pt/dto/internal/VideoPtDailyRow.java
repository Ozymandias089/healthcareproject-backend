package com.hcproj.healthcareprojectbackend.pt.dto.internal;

import java.time.Instant;

public record VideoPtDailyRow(
        String trainerNickname,
        Instant scheduledStartAt
) {}