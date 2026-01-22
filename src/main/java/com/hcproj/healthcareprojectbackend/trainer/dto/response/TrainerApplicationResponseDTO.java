package com.hcproj.healthcareprojectbackend.trainer.dto.response;

import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerApplicationStatus;

public record TrainerApplicationResponseDTO(
        String message,
        TrainerApplicationStatus applicationStatus
) {
    public static TrainerApplicationResponseDTO of(TrainerApplicationStatus status) {
        return new TrainerApplicationResponseDTO("TRAINER_DOCUMENTS_SUBMITTED", status);
    }
}