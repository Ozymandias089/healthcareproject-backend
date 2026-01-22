package com.hcproj.healthcareprojectbackend.pt.dto.request;

public record PtRoomEntryRequestDTO(
        String entryCode // 비공개 방 입장 또는 예약 시 필요한 코드
) {}