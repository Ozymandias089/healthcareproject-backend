package com.hcproj.healthcareprojectbackend.community.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 게시글/댓글 신고(Report) 정보를 나타내는 엔티티.
 *
 * <p><b>모델링 특징</b></p>
 * <ul>
 *   <li>신고자는 {@code reporterId}로 식별한다.</li>
 *   <li>신고 대상은 {@code type}({@link ReportType}) + {@code targetId}로 식별한다.</li>
 * </ul>
 *
 * <p><b>중복 신고 방지</b></p>
 * <ul>
 *   <li>{@code uk_report_reporter_target}: (reporter_id, type, target_id) 유니크</li>
 *   <li>동일 사용자가 동일 대상에 대해 중복 신고하지 못하도록 보장한다.</li>
 * </ul>
 *
 * <p><b>조회 최적화</b></p>
 * <ul>
 *   <li>{@code idx_report_status}: status 인덱스</li>
 *   <li>{@code idx_report_type}: type 인덱스</li>
 * </ul>
 *
 * <p><b>상태 전이</b></p>
 * <ul>
 *   <li>처리 완료(PROCESSED) 또는 반려(REJECTED)된 신고는 다시 변경되지 않는다.</li>
 *   <li>{@link #process()} / {@link #reject()}는 멱등(idempotent)하게 동작한다.</li>
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "reports",
        indexes = {
                @Index(name = "idx_report_status", columnList = "status"),
                @Index(name = "idx_report_type", columnList = "type")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_report_reporter_target",
                        columnNames = {"reporter_id", "type", "target_id"}
                )
        }
)
public class ReportEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    // 신고자
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    // 신고 유형 (POST / COMMENT)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ReportType type;

    // 대상 ID
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Lob
    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    /**
     * 신고를 처리 완료 상태로 변경한다.
     *
     * <p>
     * 이미 처리 완료/반려 상태인 경우 아무 동작도 하지 않는다(멱등성 보장).
     * </p>
     */
    public void process() {
        if (this.status == ReportStatus.PROCESSED || this.status == ReportStatus.REJECTED) {
            return;
        }
        this.status = ReportStatus.PROCESSED;
    }

    /**
     * 신고를 반려 상태로 변경한다.
     *
     * <p>
     * 이미 처리 완료/반려 상태인 경우 아무 동작도 하지 않는다(멱등성 보장).
     * </p>
     */
    public void reject() {
        if (this.status == ReportStatus.PROCESSED || this.status == ReportStatus.REJECTED) {
            return;
        }
        this.status = ReportStatus.REJECTED;
    }
}