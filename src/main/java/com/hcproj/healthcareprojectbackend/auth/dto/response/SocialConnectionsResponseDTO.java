package com.hcproj.healthcareprojectbackend.auth.dto.response;

import com.hcproj.healthcareprojectbackend.auth.dto.internal.SocialConnectionDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record SocialConnectionsResponseDTO(
        String handle,
        boolean hasPassword,
        List<SocialConnectionDTO> connections
) {}
