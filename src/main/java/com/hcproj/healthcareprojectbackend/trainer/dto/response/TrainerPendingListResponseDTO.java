package com.hcproj.healthcareprojectbackend.trainer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record TrainerPendingListResponseDTO(
        List<TrainerApplicantDTO> applicant, // 명세서 키: applicant
        int page,
        int size,
        long totalElements,
        boolean hasPrev,
        boolean hasNext
) {
    @Builder
    public record TrainerApplicantDTO(
            String handle,
            String nickname,
            String profileImageUrl,

            @JsonProperty("licenceUrl") // 명세서 요구사항에 맞춰 JSON 키 이름 고정
            List<String> licenseUrls,

            String bio,
            Instant createdAt
    ) {}
}