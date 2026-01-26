package com.hcproj.healthcareprojectbackend.me.dto.response;

import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerApplicationStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record TrainerInfoResponseDTO(
        TrainerApplicationStatus applicationStatus,
        List<String> licenseUrlsJson,
        String bio,
        String rejectReason,
        Instant approvedAt
) {}
