package com.hcproj.healthcareprojectbackend.trainer.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 트레이너 신청 및 승인 정보를 관리하는 엔티티.
 *
 * <p><b>관계 모델</b></p>
 * <ul>
 *   <li>{@code users} 테이블과 1:1 관계</li>
 *   <li>PK = FK 구조로 {@code user_id}가 사용자 식별자</li>
 * </ul>
 *
 * <p><b>주요 역할</b></p>
 * <ul>
 *   <li>트레이너 신청 상태 관리</li>
 *   <li>자격증/증빙 자료 URL 관리</li>
 *   <li>관리자 승인/거절 이력 관리</li>
 * </ul>
 *
 * <p><b>신청 상태 흐름</b></p>
 * <pre>
 * PENDING → APPROVED
 *        → REJECTED → (재신청 시) PENDING
 * </pre>
 *
 * <p><b>조회 최적화</b></p>
 * <ul>
 *   <li>{@code idx_trainer_status}: application_status 기준 조회 최적화</li>
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "trainer_info", indexes = {
        @Index(name = "idx_trainer_status", columnList = "application_status")
})
public class TrainerInfoEntity extends BaseTimeEntity {

    /**
     * 사용자 ID (users와 1:1, PK=FK)
     */
    @Id
    @Column(name = "user_id")
    private Long userId; // users 1:1, PK=FK

    /**
     * 트레이너 신청 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false, length = 20)
    private TrainerApplicationStatus applicationStatus; // PENDING | APPROVED | REJECTED

    /**
     * 자격증/증빙 자료 URL 목록 (JSON 문자열)
     *
     * <p>
     * 실제 구조(JSON 스키마)는 서비스 레이어에서 관리한다.
     * </p>
     */
    @Lob
    @Column(name = "license_urls_json")
    private String licenseUrlsJson;

    /**
     * 트레이너 소개 문구(Bio)
     */
    @Lob
    @Column(name = "bio")
    private String bio;

    /**
     * 관리자 거절 사유
     */
    @Lob
    @Column(name = "reject_reason")
    private String rejectReason;

    /**
     * 승인 완료 시각
     */
    @Column(name = "approved_at")
    private Instant approvedAt;

    /**
     * 트레이너 재신청 시 신청 정보를 갱신한다.
     *
     * <p>
     * <ul>
     *   <li>신청 상태를 {@link TrainerApplicationStatus#PENDING}으로 되돌린다.</li>
     *   <li>기존 거절 사유 및 승인 일시는 초기화된다.</li>
     * </ul>
     * </p>
     *
     * @param bio             새 소개 문구
     * @param licenseUrlsJson 새 자격증 URL 목록(JSON)
     */
    public void updateApplication(String bio, String licenseUrlsJson) {
        updateBio(bio);
        this.licenseUrlsJson = licenseUrlsJson;
        this.applicationStatus = TrainerApplicationStatus.PENDING; // 다시 대기 상태로 변경
        this.rejectReason = null; // 기존 거절 사유 초기화
        this.approvedAt = null;
    }

    /**
     * 트레이너 소개 문구(Bio)를 수정한다.
     *
     * <p>
     * 빈 문자열("") 입력 시 null로 정규화하여
     * 소개 문구 삭제로 처리한다.
     * </p>
     *
     * @param bio 변경할 소개 문구
     */
    public void updateBio(String bio) {
        // 빈 문자열("")인 경우 null로 설정하여 삭제 처리
        if (bio != null && bio.isEmpty()) {
            this.bio = null;
        } else {
            this.bio = bio;
        }
    }

    /**
     * 관리자 승인 처리.
     *
     * <p>
     * 상태를 {@link TrainerApplicationStatus#APPROVED}로 변경하고
     * 승인 시각을 기록한다.
     * </p>
     */
     public void approve() {
        this.applicationStatus = TrainerApplicationStatus.APPROVED;
        this.approvedAt = Instant.now();
        this.rejectReason = null; // 혹시 거절된 적이 있다면 사유 초기화
    }

    /**
     * 관리자 거절 처리.
     *
     * <p>
     * 상태를 {@link TrainerApplicationStatus#REJECTED}로 변경하고
     * 거절 사유를 기록한다.
     * </p>
     *
     * @param reason 거절 사유
     */
    public void reject(String reason) {
         this.applicationStatus = TrainerApplicationStatus.REJECTED;
         this.rejectReason = reason;
         this.approvedAt = null; // 승인된 적이 있다면 취소됨
    }
}

