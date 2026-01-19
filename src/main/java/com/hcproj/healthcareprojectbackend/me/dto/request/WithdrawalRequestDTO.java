package com.hcproj.healthcareprojectbackend.me.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WithdrawalRequestDTO(
        @NotBlank String password
) {}
