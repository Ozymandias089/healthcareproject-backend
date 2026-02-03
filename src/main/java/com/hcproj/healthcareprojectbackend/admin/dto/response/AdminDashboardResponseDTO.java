package com.hcproj.healthcareprojectbackend.admin.dto.response;

import lombok.Builder;

@Builder
public record AdminDashboardResponseDTO(
        // 1. 회원 현황
        long totalUser,      // 전체 회원
        long activeUser,     // 활성
        long inactiveUser,   // 비활성

        // 2. 게시글 현황
        long totalPost,      // 전체 게시글
        long publicPost,     // 공개
        long hiddenPost,     // 숨김

        // 3. 트레이너 신청 현황
        long waitTrainer,    // 승인 대기중

        // 4. 신고 현황 (새로 추가!)
        long waitReport,   // 처리 대기 중인 신고
        long todayReport,  // 오늘 들어온 신고

        // 5. 화상 PT 현황
        long totalPt,        // 전체
        long livePt,         // 진행중
        long reservedPt,     // 예약

        // 6. 오늘의 활동
        long todayJoin,       // 신규 가입
        long todayPost        // 오늘 새 게시글
) {}