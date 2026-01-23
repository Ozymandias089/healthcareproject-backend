package com.hcproj.healthcareprojectbackend.admin.dto.response;

import lombok.Builder;

@Builder
public record AdminDashboardResponseDTO(
        long todayJoin,   // 금일 가입자
        long waitTrainer, // 승인 대기 트레이너 (추후 구현)
        long newReport,   // 미처리 신고 (추후 구현)

        // [수정됨] 화상 PT 카드 데이터
        long totalPt,      // 전체 (진행중 + 예약)
        long livePt,       // 진행중
        long reservedPt    // 예약
) {}