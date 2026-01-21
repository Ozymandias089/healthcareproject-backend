package com.hcproj.healthcareprojectbackend.workout.dto.response;

import java.util.List;

public record ExerciseDetailResponseDTO(
        ExerciseDTO exercise,
        List<AlternativeExerciseDTO> alternatives
) {

    public record ExerciseDTO(
            Long exerciseId,
            String name,
            String imageUrl,
            String description,
            String bodyPart,
            String difficulty,
            String precautions,
            String youtubeUrl
    ) {}
}