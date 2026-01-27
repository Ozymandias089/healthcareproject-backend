package com.hcproj.healthcareprojectbackend.workout.ai;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 운동 루틴 생성 AI 프롬프트 템플릿.
 *
 * <p><b>설계 의도</b></p>
 * <ul>
 *   <li>AI가 <b>오직 JSON만</b> 출력하도록 강제한다(마크다운/설명 금지).</li>
 *   <li>서버가 제공한 allowedExercises의 exerciseId만 사용하도록 규칙을 명시한다.</li>
 * </ul>
 *
 * <p><b>출력 계약</b></p>
 * <ul>
 *   <li>시스템 프롬프트에 JSON Schema를 포함하여 파싱 가능한 구조를 강제한다.</li>
 *   <li>요청 날짜 외의 날짜 루틴을 생성하지 않도록 제한한다.</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkoutAiPrompts {

    /**
     * 시스템 프롬프트.
     *
     * <p>
     * JSON-only 출력 및 allowlist 기반 exerciseId 사용을 강제한다.
     * </p>
     */
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
      "totalMinutes":integer,
      "items":[
        {
          "displayOrder":integer,
          "exerciseId":integer,
          "sets":integer|null,
          "reps":integer|null,
          "restSecond":integer|null,
          "durationMinutes":integer|null,
          "distanceKm":number|null,
          "rpe":integer|null,
          "amount":string|null
        }
      ]
    }
  ]
}

Rules:
- Generate routines ONLY for the given dates (no extra dates, no missing dates, no duplicates).
- days must contain exactly the same dates as the input dates.
- For each day, items must not be empty.
- displayOrder starts at 0 and is contiguous per day.
- totalMinutes must be a reasonable estimate between 30 and 90 (integer).
- considerations must be an array of 2 to 3 Korean sentences.

Language rules:
- title must be Korean and must not contain English letters (A-Z, a-z)
- considerations must be Korean and must not contain English letters (A-Z, a-z)
- amount (if present) must be Korean and must not contain English letters (A-Z, a-z)
""";

    /**
     * 유저 프롬프트를 생성한다.
     *
     * <p>
     * additionalRequest는 null/blank이면 "NONE"으로 정규화하여 프롬프트 내에 포함한다.
     * allowedExercisesJson은 JSON 배열 문자열이며, AI는 이 목록에서만 선택해야 한다.
     * </p>
     *
     * @param dates              생성 대상 날짜 목록
     * @param additionalRequest  추가 요청(선택)
     * @param allowedExercisesJson 허용 운동 목록(JSON 배열 문자열)
     * @return 최종 user prompt 문자열
     */
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
