package com.hcproj.healthcareprojectbackend.pt.dto.request;

public record PtRoomJoinRequestDTO(
        String entryCode // 비공개 방일 경우 필수
) {}