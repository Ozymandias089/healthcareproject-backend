package com.hcproj.healthcareprojectbackend.admin.dto.request;

public record UserStatusUpdateRequestDTO(
        String status // "BANNED" or "ACTIVE"
) {}