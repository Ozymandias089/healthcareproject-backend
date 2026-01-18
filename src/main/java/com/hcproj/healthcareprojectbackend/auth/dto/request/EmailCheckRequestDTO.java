package com.hcproj.healthcareprojectbackend.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailCheckRequestDTO(
        @Email @NotBlank String email
) {}
