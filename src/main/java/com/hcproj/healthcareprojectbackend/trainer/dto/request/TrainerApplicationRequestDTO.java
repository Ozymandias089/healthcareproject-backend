package com.hcproj.healthcareprojectbackend.trainer.dto.request;

import jakarta.validation.constraints.Size;
import java.util.List;

public record TrainerApplicationRequestDTO(
        @Size(max = 5, message = "자격증 파일은 최대 5개까지 등록 가능합니다.")
        List<String> licenseUrls,
        String bio
) {}