package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminDashboardResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.repository.AdminPtRoomRepository;
import com.hcproj.healthcareprojectbackend.admin.repository.AdminUserRepository;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerApplicationStatus;
import com.hcproj.healthcareprojectbackend.trainer.repository.TrainerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AdminUserRepository adminUserRepository;
    private final AdminPtRoomRepository adminPtRoomRepository;
    private final TrainerInfoRepository trainerInfoRepository;
    private final PtRoomRepository ptRoomRepository;

    public AdminDashboardResponseDTO getDashboardData() {
        // 1. 금일 가입자 수
        var startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        long todayJoin = adminUserRepository.countByCreatedAtAfter(startOfToday);

        // 2. 승인 대기 트레이너 수 (실제 DB 조회 연동 완료)
        long waitTrainer = trainerInfoRepository.countByApplicationStatus(TrainerApplicationStatus.PENDING);
        long newReport = 0;

        // 2. 현재 라이브 방
        long livePt = ptRoomRepository.countByStatus(PtRoomStatus.LIVE);           // 진행중
        long reservedPt = ptRoomRepository.countByStatus(PtRoomStatus.SCHEDULED);  // 예약
        long totalPt = livePt + reservedPt; // 전체 = 진행중 + 예약

        return AdminDashboardResponseDTO.builder()
                .todayJoin(todayJoin)
                .waitTrainer(waitTrainer)
                .newReport(newReport)
                .totalPt(totalPt)
                .livePt(livePt)         // [변경] liveRoom -> livePt
                .reservedPt(reservedPt)
                .build();
    }
}