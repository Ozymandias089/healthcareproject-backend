package com.hcproj.healthcareprojectbackend.me.dto.request;

import com.hcproj.healthcareprojectbackend.me.dto.internal.InjuriesRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.internal.ProfileDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OnboardingRequestDTO(
        @NotNull
        @Valid
        ProfileDTO profile,

        @Valid
        List<InjuriesRequestDTO> injuries,

        List<String> allergies
) {}
