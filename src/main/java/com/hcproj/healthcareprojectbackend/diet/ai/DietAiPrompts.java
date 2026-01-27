package com.hcproj.healthcareprojectbackend.diet.ai;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 주간 식단 계획 생성 AI 프롬프트 템플릿.
 *
 * <p><b>설계 의도</b></p>
 * <ul>
 *   <li>AI가 오직 JSON만 출력하도록 강제한다(마크다운/코드펜스/설명 금지).</li>
 *   <li>서버가 제공한 allowedFoods의 foodId만 사용하도록 제한하여 데이터 정합성을 보장한다.</li>
 * </ul>
 *
 * <p><b>출력 계약</b></p>
 * <ul>
 *   <li>startDate..startDate+6 범위의 정확히 7일을 생성한다.</li>
 *   <li>하루 3~5끼, displayOrder는 0부터 연속.</li>
 *   <li>count는 양의 정수.</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DietAiPrompts {

    /**
     * 시스템 프롬프트.
     *
     * <p>
     * JSON-only 출력 및 allowlist 기반 foodId 사용을 강제한다.
     * </p>
     */
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

    /**
     * 유저 프롬프트를 생성한다.
     *
     * <p>
     * note는 null/blank이면 "NONE"으로 정규화한다.
     * allergies는 null이면 "[]"로 표현한다(프롬프트 전달용).
     * </p>
     *
     * @param startDate        주간 시작일
     * @param allergies        알레르기 코드 목록(선택)
     * @param note             추가 요청/주의사항(선택)
     * @param allowedFoodsJson 허용 음식 목록(JSON 배열 문자열)
     * @return 최종 user prompt 문자열
     */
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
