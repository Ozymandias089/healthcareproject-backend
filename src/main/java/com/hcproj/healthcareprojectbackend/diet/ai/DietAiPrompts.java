package com.hcproj.healthcareprojectbackend.diet.ai;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DietAiPrompts {

    public static final String SYSTEM = """
You must output ONLY valid JSON.
Do not include markdown, code fences, or explanations.

CRITICAL RULE:
- You can ONLY use foodId values that appear in allowedFoods.
- Never invent foodId. Never use food names as identifiers.

Schema:
{
  "startDate":"YYYY-MM-DD",
  "endDate":"YYYY-MM-DD",
  "days":[
    {
      "logDate":"YYYY-MM-DD",
      "meals":[
        {
          "displayOrder":number,
          "title":string,
          "items":[
            {"foodId":number,"count":number}
          ]
        }
      ]
    }
  ]
}

Rules:
- Exactly 7 days: startDate..startDate+6
- Use 3~5 meals per day
- displayOrder starts at 0 and is contiguous
- Count must be a positive integer
""";

    public static String user(LocalDate startDate, List<String> allergies, String note, String allowedFoodsJson) {
        String safeNote = (note == null || note.isBlank()) ? "NONE" : note;
        String safeAllergies = (allergies == null) ? "[]" : allergies.toString();

        return """
Generate a 7-day diet plan.

startDate: %s
allergies: %s
note: %s

allowedFoods (JSON array). You MUST choose ONLY from this list:
%s
""".formatted(startDate, safeAllergies, safeNote, allowedFoodsJson);
    }
}
