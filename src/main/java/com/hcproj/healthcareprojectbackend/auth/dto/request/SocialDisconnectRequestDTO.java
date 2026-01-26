package com.hcproj.healthcareprojectbackend.auth.dto.request;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;
import jakarta.validation.constraints.NotNull;

public record SocialDisconnectRequestDTO(@NotNull SocialProvider provider) {}