package com.hcproj.healthcareprojectbackend.calendar.dto.request;

import jakarta.validation.constraints.NotNull;

public record PutMemoRequestDTO(
        @NotNull String content
) {}
