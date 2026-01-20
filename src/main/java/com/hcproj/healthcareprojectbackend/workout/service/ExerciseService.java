package com.hcproj.healthcareprojectbackend.workout.service;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.workout.dto.response.AlternativeExerciseDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.entity.ExerciseEntity;
import com.hcproj.healthcareprojectbackend.workout.repository.ExerciseRepository;
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
}

