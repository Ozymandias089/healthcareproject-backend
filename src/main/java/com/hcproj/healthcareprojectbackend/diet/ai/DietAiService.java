package com.hcproj.healthcareprojectbackend.diet.ai;

import com.hcproj.healthcareprojectbackend.diet.ai.dto.DietAiWeekPlanResult;
import com.hcproj.healthcareprojectbackend.global.ai.AiJsonCaller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietAiService {

    private final AiJsonCaller aiJsonCaller;
    private final DietAiFoodCatalogService foodCatalogService;

    public DietAiWeekPlanResult generateWeekPlan(LocalDate startDate, List<String> allergies, String note) {
        // allowedFoods 최대 개수: 토큰/품질 밸런스
        var payload = foodCatalogService.buildAllowedFoodsPayload(allergies, 120);

        return aiJsonCaller.callJson(
                DietAiPrompts.SYSTEM,
                DietAiPrompts.user(startDate, allergies, note, payload.allowedFoodsJson()),
                DietAiWeekPlanResult.class
        );
    }
}

