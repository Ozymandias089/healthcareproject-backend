package com.hcproj.healthcareprojectbackend.diet.controller;

import com.hcproj.healthcareprojectbackend.diet.service.DietAiWeekPlanFacade;
import com.hcproj.healthcareprojectbackend.diet.ai.dto.AiDietWeekPlanPutRequestDTO;
import com.hcproj.healthcareprojectbackend.diet.dto.response.AiDietWeekPlanResponseDTO;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DietAiController {

    private final DietAiWeekPlanFacade dietAiWeekPlanFacade;

    @PutMapping("/diets/ai/week-plans")
    public ApiResponse<AiDietWeekPlanResponseDTO> replaceWeekPlans(
            @CurrentUserId Long userId,
            @Valid @RequestBody AiDietWeekPlanPutRequestDTO request
    ) {
        return ApiResponse.ok(dietAiWeekPlanFacade.replaceWeekPlan(userId, request));
    }
}
