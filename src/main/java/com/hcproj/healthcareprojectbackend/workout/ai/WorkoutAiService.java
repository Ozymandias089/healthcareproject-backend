package com.hcproj.healthcareprojectbackend.workout.ai;

import com.hcproj.healthcareprojectbackend.global.ai.AiJsonCaller;
import com.hcproj.healthcareprojectbackend.workout.ai.dto.WorkoutAiRoutineResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 운동 루틴 생성 AI 유스케이스 서비스.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>지정된 날짜 목록에 대한 운동 루틴을 AI로 생성한다.</li>
 *   <li>AI가 선택할 수 있는 운동 목록을 서버가 제공(allowlist)하여 안전하게 제약한다.</li>
 *   <li>AI 출력(JSON)을 {@link WorkoutAiRoutineResult}로 파싱하여 호출자에 반환한다.</li>
 * </ul>
 *
 * <p><b>안전 장치</b></p>
 * <ul>
 *   <li>{@link WorkoutAiExerciseCatalogService}가 제공하는 allowedExercises 목록 안에서만 exerciseId를 선택하도록 프롬프트로 강제한다.</li>
 *   <li>{@link AiJsonCaller}는 JSON 이외 출력 또는 파싱 실패 시 프로젝트 예외로 래핑한다.</li>
 * </ul>
 *
 * <p><b>반환</b></p>
 * <ul>
 *   <li>AI 결과({@link WorkoutAiRoutineResult})</li>
 *   <li>AI에게 제공한 허용 운동 목록(payload) - 디버깅/재현성 확보용</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class WorkoutAiService {

    private final AiJsonCaller aiJsonCaller;
    private final WorkoutAiExerciseCatalogService catalogService;

    private final WorkoutAiPrompts workoutAiPrompts;

    /**
     * 지정된 날짜들에 대한 운동 루틴을 생성한다.
     *
     * @param dates             루틴을 생성할 날짜 목록
     * @param additionalRequest 추가 요청(선택). null/blank이면 NONE으로 처리한다.
     * @return 생성 결과 + 허용 운동 목록 payload
     */
    public Generated generate(List<LocalDate> dates, String additionalRequest) {
        var payload = catalogService.buildAllowedExercisesPayload(180);

        WorkoutAiRoutineResult result = aiJsonCaller.callJson(
                workoutAiPrompts.system(),
                workoutAiPrompts.user(dates, additionalRequest, payload.allowedExercisesJson()),
                WorkoutAiRoutineResult.class
        );

        return new Generated(result, payload);
    }

    /**
     * 루틴 생성 결과와, AI 입력으로 사용한 허용 운동 목록을 함께 묶은 반환 타입.
     *
     * @param result         AI 생성 루틴 결과
     * @param allowedPayload AI에게 제공한 허용 운동 목록(재현/디버깅 목적)
     */
    public record Generated(
            WorkoutAiRoutineResult result,
            WorkoutAiExerciseCatalogService.AllowedExercisesPayload allowedPayload
    ) {}
}
