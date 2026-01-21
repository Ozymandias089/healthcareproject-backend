package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminDashboardResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.repository.AdminPtRoomRepository;
import com.hcproj.healthcareprojectbackend.admin.repository.AdminUserRepository;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardFacade {

    private final AdminUserRepository adminUserRepository;
    private final AdminPtRoomRepository adminPtRoomRepository;

    public AdminDashboardResponseDTO getDashboardData() {
        // 1. 금일 가입자 수
        var startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        long todayJoin = adminUserRepository.countByCreatedAtAfter(startOfToday);

        // 2. 현재 라이브 방
        long liveRoom = adminPtRoomRepository.countByStatus(PtRoomStatus.LIVE);

        // 3. 기타 (미구현 상태는 0)
        long waitTrainer = 0;
        long newReport = 0;

        return AdminDashboardResponseDTO.builder()
                .todayJoin(todayJoin)
                .waitTrainer(waitTrainer)
                .newReport(newReport)
                .liveRoom(liveRoom)
                .build();
    }
}