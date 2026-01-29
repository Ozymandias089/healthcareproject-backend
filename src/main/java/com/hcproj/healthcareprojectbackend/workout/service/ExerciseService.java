package com.hcproj.healthcareprojectbackend.workout.service;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.util.UtilityProvider;
import com.hcproj.healthcareprojectbackend.workout.dto.response.AlternativeExerciseDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import com.hcproj.healthcareprojectbackend.workout.repository.ExerciseRepository;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseListResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.request.ExerciseCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseCreateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 운동(Exercise) 관련 비즈니스 로직을 처리하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    /**
     * 운동 상세 조회 (대체 운동 3개 포함).
     */
    @Transactional(readOnly = true)
    public ExerciseDetailResponseDTO getExerciseDetail(Long exerciseId) {
        ExerciseEntity exercise = exerciseRepository.findByExerciseIdAndIsActiveTrue(exerciseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXERCISE_NOT_FOUND));

        List<AlternativeExerciseDTO> alternatives = findAlternatives(exercise);

        ExerciseDetailResponseDTO.ExerciseDTO exerciseDTO = new ExerciseDetailResponseDTO.ExerciseDTO(
                exercise.getExerciseId(),
                exercise.getName(),
                exercise.getImageUrl(),
                exercise.getDescription(),
                exercise.getBodyPart(),
                exercise.getDifficulty(),
                exercise.getPrecautions(),
                exercise.getYoutubeUrl()
        );

        return new ExerciseDetailResponseDTO(exerciseDTO, alternatives);
    }

    /**
     * 대체 운동 목록 조회.
     */
    private List<AlternativeExerciseDTO> findAlternatives(ExerciseEntity exercise) {
        if (exercise.getBodyPart() == null || exercise.getBodyPart().isBlank()) {
            return Collections.emptyList();
        }

        List<ExerciseEntity> alternativeEntities = exerciseRepository.findAlternatives(
                exercise.getBodyPart(),
                exercise.getExerciseId()
        );

        return alternativeEntities.stream()
                .map(entity -> new AlternativeExerciseDTO(
                        entity.getExerciseId(),
                        entity.getName(),
                        entity.getImageUrl()
                ))
                .toList();
    }

    /**
     * 운동 리스트 조회 (무한 스크롤 + 검색 + 필터 + 띄어쓰기 무시).
     */
    @Transactional(readOnly = true)
    public ExerciseListResponseDTO getExerciseList(Long cursor, Integer limit, String keyword, String bodyPart) {
        // 1. limit 유효성 검사
        int actualLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int limitSize = actualLimit + 1; // hasNext 판단용

        // 2. 검색어 정규화
        String normalizedKeyword = UtilityProvider.normalizeKeyword(keyword);

        // 3. bodyPart 정규화
        String normalizedBodyPart = UtilityProvider.normalizeBodyPart(bodyPart);

        // 4. 조회 (검색어 유무에 따라 분기)
        List<ExerciseEntity> entities;
        if (normalizedKeyword == null) {
            // 검색어 없음
            entities = exerciseRepository.findExercisesWithCursorNoKeyword(
                    cursor, normalizedBodyPart, limitSize
            );
        } else {
            // 검색어 있음 - 띄어쓰기 제거 + 소문자 변환 + 와일드카드 추가
            String likePattern = "%" + normalizedKeyword.toLowerCase().replace(" ", "") + "%";
            entities = exerciseRepository.findExercisesWithCursorAndKeyword(
                    cursor, likePattern, normalizedBodyPart, limitSize
            );
        }

        // 5. hasNext 판단
        boolean hasNext = entities.size() > actualLimit;

        // 6. 실제 반환할 데이터 (limit개만)
        List<ExerciseEntity> resultEntities = hasNext
                ? entities.subList(0, actualLimit)
                : entities;

        // 7. DTO 변환
        List<ExerciseListResponseDTO.ExerciseItemDTO> items = resultEntities.stream()
                .map(entity -> new ExerciseListResponseDTO.ExerciseItemDTO(
                        entity.getExerciseId(),
                        entity.getName(),
                        entity.getImageUrl(),
                        entity.getBodyPart(),
                        entity.getDifficulty(),
                        entity.getYoutubeUrl()
                ))
                .toList();

        // 8. nextCursor 계산
        Long nextCursor = hasNext && !resultEntities.isEmpty()
                ? resultEntities.get(resultEntities.size() - 1).getExerciseId()
                : null;

        return new ExerciseListResponseDTO(items, nextCursor, hasNext);
    }

    /**
     * 운동 등록 (관리자 전용)
     */
    @Transactional
    public ExerciseCreateResponseDTO createExercise(ExerciseCreateRequestDTO request) {
        ExerciseEntity exercise = ExerciseEntity.builder()
                .name(request.name())
                .bodyPart(request.bodyPart())
                .difficulty(request.difficulty())
                .imageUrl(request.imageUrl())
                .description(request.description())
                .precautions(request.precautions())
                .youtubeUrl(request.youtubeUrl())
                .isActive(request.isActive())
                .build();

        ExerciseEntity saved = exerciseRepository.save(exercise);

        return ExerciseCreateResponseDTO.builder()
                .exerciseId(saved.getExerciseId())
                .name(saved.getName())
                .isActive(saved.getIsActive())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * 운동 삭제 (관리자 전용)
     */
    @Transactional
    public ExerciseCreateResponseDTO deleteExercise(Long exerciseId) {
        ExerciseEntity exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXERCISE_NOT_FOUND));

        exerciseRepository.delete(exercise);

        return ExerciseCreateResponseDTO.builder()
                .exerciseId(exerciseId)
                .message("운동이 삭제되었습니다.")
                .build();
    }
}