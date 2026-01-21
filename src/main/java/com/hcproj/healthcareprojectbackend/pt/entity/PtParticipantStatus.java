package com.hcproj.healthcareprojectbackend.pt.entity;

public enum PtParticipantStatus {
    SCHEDULED, // 대기
    JOINED,    // 입장함
    LEFT,      // 퇴장함
    KICKED,    // 강퇴됨
    CANCELLED  // 취소됨
}