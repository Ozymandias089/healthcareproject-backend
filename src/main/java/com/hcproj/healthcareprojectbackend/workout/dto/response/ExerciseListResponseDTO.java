package com.hcproj.healthcareprojectbackend.workout.dto.response;

import java.util.List;

public record ExerciseListResponseDTO(
        List<ExerciseItemDTO> items,
        Long nextCursor,
        boolean hasNext
) {
    public record ExerciseItemDTO(
            Long exerciseId,
            String name,
            String imageUrl,
            String bodyPart,
            String difficulty,
            String youtubeUrl
    ) {}
}