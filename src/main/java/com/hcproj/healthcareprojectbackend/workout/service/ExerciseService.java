package com.hcproj.healthcareprojectbackend.workout.service;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.workout.dto.response.AlternativeExerciseDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import com.hcproj.healthcareprojectbackend.workout.repository.ExerciseRepository;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseListResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.request.ExerciseCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseCreateResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        // 1. 운동 조회 (활성화된 운동만)
        ExerciseEntity exercise = exerciseRepository.findByExerciseIdAndIsActiveTrue(exerciseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXERCISE_NOT_FOUND));

        // 2. 대체 운동 조회
        List<AlternativeExerciseDTO> alternatives = findAlternatives(exercise);

        // 3. 응답 DTO 생성
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
                exercise.getExerciseId(),
                PageRequest.of(0, 3)
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
     * 운동 리스트 조회 (무한 스크롤 + 검색 + 필터).
     */
    @Transactional(readOnly = true)
    public ExerciseListResponseDTO getExerciseList(Long cursor, Integer limit, String keyword, String bodyPart) {
        int actualLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        Pageable pageable = PageRequest.of(0, actualLimit + 1);

        List<ExerciseEntity> entities = exerciseRepository.findExercisesWithCursor(
                cursor,
                keyword,
                bodyPart,
                pageable
        );

        boolean hasNext = entities.size() > actualLimit;

        List<ExerciseEntity> resultEntities = hasNext
                ? entities.subList(0, actualLimit)
                : entities;

        var items = resultEntities.stream()
                .map(entity -> new ExerciseListResponseDTO.ExerciseItemDTO(
                        entity.getExerciseId(),
                        entity.getName(),
                        entity.getImageUrl(),
                        entity.getBodyPart(),
                        entity.getDifficulty(),
                        entity.getYoutubeUrl()
                ))
                .toList();

        Long nextCursor = hasNext && !resultEntities.isEmpty()
                ? resultEntities.getLast().getExerciseId()
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

