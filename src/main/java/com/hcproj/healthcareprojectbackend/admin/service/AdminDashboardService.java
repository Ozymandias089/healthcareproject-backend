package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminDashboardResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerApplicationStatus;
import com.hcproj.healthcareprojectbackend.trainer.repository.TrainerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final TrainerInfoRepository trainerInfoRepository;
    private final PtRoomRepository ptRoomRepository;
    private final PostRepository postRepository;

    public AdminDashboardResponseDTO getDashboardData() {
        // [날짜 계산] 오늘 00:00:00 (시스템 타임존 기준)
        Instant startOfToday = Instant.now()
                .atZone(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();

        // 1. 회원 현황
        // 전체 회원 수 (DB 전체 카운트)
        long totalUser = userRepository.count();
        // 활성 회원 수
        long activeUser = userRepository.countByStatus(UserStatus.ACTIVE);
        // [수정됨] 비활성 회원 = 전체 - 활성 (정지, 탈퇴 등 포함)
        long inactiveUser = totalUser - activeUser;

        // 2. 게시글 현황
        long publicPost = postRepository.countByStatus(PostStatus.POSTED);
        long hiddenPost = postRepository.countByStatus(PostStatus.DELETED);
        long totalPost = publicPost + hiddenPost;

        // 3. 트레이너 신청 현황 (대기중)
        long waitTrainer = trainerInfoRepository.countByApplicationStatus(TrainerApplicationStatus.PENDING);

        // 4. 화상 PT 현황
        long livePt = ptRoomRepository.countByStatus(PtRoomStatus.LIVE);
        long reservedPt = ptRoomRepository.countByStatus(PtRoomStatus.SCHEDULED);
        long totalPt = livePt + reservedPt;

        // 5. 오늘의 활동
        long todayJoin = userRepository.countByCreatedAtAfter(startOfToday);
        long todayTrainerApp = trainerInfoRepository.countByCreatedAtAfter(startOfToday);
        long todayPost = postRepository.countByCreatedAtAfter(startOfToday);

        return AdminDashboardResponseDTO.builder()
                .totalUser(totalUser)
                .activeUser(activeUser)
                .inactiveUser(inactiveUser)
                .totalPost(totalPost)
                .publicPost(publicPost)
                .hiddenPost(hiddenPost)
                .waitTrainer(waitTrainer)
                .totalPt(totalPt)
                .livePt(livePt)
                .reservedPt(reservedPt)
                .todayJoin(todayJoin)
                .todayPost(todayPost)
                .build();
    }
}