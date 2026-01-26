package com.hcproj.healthcareprojectbackend.diet.ai.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record AiDietWeekPlanPutRequestDTO(
        List<String> allergies,
        @Size(max = 500)
        String note
) {}