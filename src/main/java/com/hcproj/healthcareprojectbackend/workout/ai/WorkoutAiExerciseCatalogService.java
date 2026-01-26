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

@Service
@RequiredArgsConstructor
public class WorkoutAiExerciseCatalogService {

    private final ExerciseRepository exerciseRepository;
    private final ObjectMapper objectMapper;

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

    public record AllowedExerciseDTO(Long id, String name, String bodyPart, String difficulty) {}
    public record AllowedExercisesPayload(List<AllowedExerciseDTO> allowedExercises, String allowedExercisesJson) {}
}
