package com.hcproj.healthcareprojectbackend.trainer.dto.request;

public record TrainerBioUpdateRequestDTO(
        String bio // 빈 문자열 허용 (삭제 의미)이므로 @NotBlank 붙이지 않음
) {}