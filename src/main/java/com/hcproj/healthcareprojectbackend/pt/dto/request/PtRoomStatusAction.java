package com.hcproj.healthcareprojectbackend.pt.dto.request;

public enum PtRoomStatusAction {
    START, // 시작 (SCHEDULED -> LIVE)
    END    // 종료 (LIVE -> ENDED)
}