package com.hcproj.healthcareprojectbackend.workout.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import com.hcproj.healthcareprojectbackend.workout.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 루틴 생성에 사용할 "허용 운동 목록(allowlist)"을 구성하는 서비스.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>활성화된 운동({@code isActive=true}) 중 일부를 조회하여 AI 입력으로 제공한다.</li>
 *   <li>허용 목록을 DTO로 변환하고 JSON 문자열로 직렬화하여 프롬프트에 포함할 수 있게 한다.</li>
 * </ul>
 *
 * <p><b>용량 제한</b></p>
 * <ul>
 *   <li>AI 입력 토큰 비용을 고려하여 조회 개수를 제한한다.</li>
 *   <li>현재 구현은 최소 20, 최대 250 범위로 clamp한다.</li>
 * </ul>
 *
 * <p><b>예외</b></p>
 * <ul>
 *   <li>직렬화 실패 시 {@link BusinessException}으로 래핑한다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class WorkoutAiExerciseCatalogService {

    private final ExerciseRepository exerciseRepository;
    private final ObjectMapper objectMapper;

    /**
     * 허용 운동 목록 payload를 생성한다.
     *
     * <p>
     * 반환값에는 DTO 리스트와, 동일 데이터를 JSON 문자열로 직렬화한 값이 함께 포함된다.
     * </p>
     *
     * @param max 요청 최대치(입력). 내부 정책에 따라 20~250 사이로 조정된다.
     * @return 허용 운동 목록 payload
     * @throws BusinessException JSON 직렬화 실패 시
     */
    public AllowedExercisesPayload buildAllowedExercisesPayload(int max) {
        int size = Math.max(20, Math.min(max, 250));
        List<ExerciseEntity> ex = exerciseRepository.findByIsActiveTrue(PageRequest.of(0, size));

        List<AllowedExerciseDTO> allowed = ex.stream()
                .map(e -> new AllowedExerciseDTO(e.getExerciseId(), e.getName(), e.getBodyPart(), e.getDifficulty()))
                .toList();

        try {
            return new AllowedExercisesPayload(allowed, objectMapper.writeValueAsString(allowed));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_ALLOWED_EXERCISES_BUILD_FAILED);
        }
    }

    /**
     * AI에게 제공할 허용 운동 DTO.
     *
     * @param id         운동 ID (exerciseId)
     * @param name       운동 이름
     * @param bodyPart   운동 부위(문자열)
     * @param difficulty 난이도(문자열)
     */
    public record AllowedExerciseDTO(Long id, String name, String bodyPart, String difficulty) {}

    /**
     * 허용 운동 목록 payload.
     *
     * @param allowedExercises 허용 운동 DTO 리스트
     * @param allowedExercisesJson 동일 데이터의 JSON 배열 문자열(프롬프트 삽입용)
     */
    public record AllowedExercisesPayload(List<AllowedExerciseDTO> allowedExercises, String allowedExercisesJson) {}
}
