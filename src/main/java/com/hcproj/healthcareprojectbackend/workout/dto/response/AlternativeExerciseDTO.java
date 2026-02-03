package com.hcproj.healthcareprojectbackend.workout.dto.response;

public record AlternativeExerciseDTO(
        Long exerciseId,
        String name,
        String imageUrl
) {}