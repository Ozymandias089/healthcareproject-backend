package com.hcproj.healthcareprojectbackend.me.dto.internal;

import jakarta.validation.constraints.NotBlank;

public record InjuriesRequestDTO(
        @NotBlank String injuryPart,
        @NotBlank String injuryLevel
) {}
