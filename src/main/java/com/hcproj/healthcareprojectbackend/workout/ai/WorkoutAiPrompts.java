package com.hcproj.healthcareprojectbackend.workout.ai;

import com.hcproj.healthcareprojectbackend.global.ai.PromptLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkoutAiPrompts {

    private final PromptLoader promptLoader;

    @Value("${ai.prompt.workout.routine.system}")
    private String systemPath;

    @Value("${ai.prompt.workout.routine.user}")
    private String userPath;

    public String system() {
        return promptLoader.loadClasspath(systemPath);
    }

    public String user(List<LocalDate> dates, String additionalRequest, String allowedExercisesJson) {
        String template = promptLoader.loadClasspath(userPath);
        String safeReq = (additionalRequest == null || additionalRequest.isBlank()) ? "NONE" : additionalRequest;
        return template.formatted(dates, safeReq, allowedExercisesJson);
    }
}
