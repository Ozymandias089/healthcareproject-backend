package com.hcproj.healthcareprojectbackend.workout.ai;

import com.hcproj.healthcareprojectbackend.global.ai.AiJsonCaller;
import com.hcproj.healthcareprojectbackend.workout.ai.dto.WorkoutAiRoutineResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutAiService {

    private final AiJsonCaller aiJsonCaller;
    private final WorkoutAiExerciseCatalogService catalogService;

    public Generated generate(List<LocalDate> dates, String additionalRequest) {
        var payload = catalogService.buildAllowedExercisesPayload(180);

        WorkoutAiRoutineResult result = aiJsonCaller.callJson(
                WorkoutAiPrompts.SYSTEM,
                WorkoutAiPrompts.user(dates, additionalRequest, payload.allowedExercisesJson()),
                WorkoutAiRoutineResult.class
        );

        return new Generated(result, payload);
    }

    public record Generated(
            WorkoutAiRoutineResult result,
            WorkoutAiExerciseCatalogService.AllowedExercisesPayload allowedPayload
    ) {}
}
