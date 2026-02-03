package com.hcproj.healthcareprojectbackend.pt.entity;

/** PT 방 참가 상태. */
public enum PtParticipantStatus {
    JOINED,    // 입장함
    LEFT,      // 퇴장함
    KICKED,    // 강퇴됨
    CANCELLED  // 취소됨
}