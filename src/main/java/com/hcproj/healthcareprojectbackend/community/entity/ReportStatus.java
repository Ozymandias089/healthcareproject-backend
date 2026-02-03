package com.hcproj.healthcareprojectbackend.community.entity;

public enum ReportStatus {
    PENDING,    // 대기중 (신고 접수됨)
    PROCESSED,  // 처리됨 (삭제/블라인드 조치)
    REJECTED    // 반려됨 (문제 없음)
}