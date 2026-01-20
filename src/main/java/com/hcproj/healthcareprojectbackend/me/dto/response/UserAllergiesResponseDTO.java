package com.hcproj.healthcareprojectbackend.me.dto.response;

import java.util.List;

public record UserAllergiesResponseDTO(
        int count,
        List<String> allergies
) {}