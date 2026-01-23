package com.hcproj.healthcareprojectbackend.trainer.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "trainer_info", indexes = {
        @Index(name = "idx_trainer_status", columnList = "application_status")
})
public class TrainerInfoEntity extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private Long userId; // users 1:1, PK=FK

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false, length = 20)
    private TrainerApplicationStatus applicationStatus; // PENDING | APPROVED | REJECTED

    @Lob
    @Column(name = "license_urls_json")
    private String licenseUrlsJson;

    @Lob
    @Column(name = "bio")
    private String bio;

    @Lob
    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "approved_at")
    private Instant approvedAt;

    // 트레이너 재신청 시 정보를 갱신하는 메서드
    public void updateApplication(String bio, String licenseUrlsJson) {
        this.bio = bio;
        this.licenseUrlsJson = licenseUrlsJson;
        this.applicationStatus = TrainerApplicationStatus.PENDING; // 다시 대기 상태로 변경
        this.rejectReason = null; // 기존 거절 사유 초기화
        this.approvedAt = null;
    }

    // 소개문구(Bio) 수정 메서드
    public void updateBio(String bio) {
        // 빈 문자열("")인 경우 null로 설정하여 삭제 처리
        if (bio != null && bio.isEmpty()) {
            this.bio = null;
        } else {
            this.bio = bio;
        }
    }
}
