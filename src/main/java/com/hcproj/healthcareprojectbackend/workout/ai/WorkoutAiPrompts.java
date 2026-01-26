package com.hcproj.healthcareprojectbackend.workout.ai;

import java.time.LocalDate;
import java.util.List;

public final class WorkoutAiPrompts {
    private WorkoutAiPrompts() {}

    public static final String SYSTEM = """
You must output ONLY valid JSON. No markdown, no code fences, no explanations.

CRITICAL RULE:
- You can ONLY use exerciseId values that appear in allowedExercises.
- Never invent exerciseId.

Schema:
{
  "considerations":[string],
  "days":[
    {
      "logDate":"YYYY-MM-DD",
      "title":string,
      "totalMinutes":number,
      "items":[
        {
          "displayOrder":number,
          "exerciseId":number,
          "sets":number|null,
          "reps":number|null,
          "restSecond":number|null,
          "durationMinutes":number|null,
          "distanceKm":number|null,
          "rpe":number|null,
          "amount":string|null
        }
      ]
    }
  ]
}

Rules:
- Generate routines ONLY for the given dates.
- displayOrder starts at 0 and is contiguous per day.
- totalMinutes should be a reasonable estimate (30~90).
""";

    public static String user(List<LocalDate> dates, String additionalRequest, String allowedExercisesJson) {
        String safeReq = (additionalRequest == null || additionalRequest.isBlank()) ? "NONE" : additionalRequest;

        return """
Generate workout routines.

dates: %s
additionalRequest: %s

allowedExercises (JSON array). You MUST choose ONLY from this list:
%s
""".formatted(dates, safeReq, allowedExercisesJson);
    }
}
