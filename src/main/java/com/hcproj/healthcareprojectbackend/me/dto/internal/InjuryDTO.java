package com.hcproj.healthcareprojectbackend.me.dto.internal;

import lombok.Builder;

import java.time.Instant;

@Builder
public record InjuryDTO(
        long injuryId,
        String injuryPart,
        String injuryLevel,
        Instant createdAt,
        Instant updatedAt
) {}
