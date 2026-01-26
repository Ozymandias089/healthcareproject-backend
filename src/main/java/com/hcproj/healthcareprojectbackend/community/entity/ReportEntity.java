package com.hcproj.healthcareprojectbackend.community.entity;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

    // 신고 처리
    public void process() {
        if (this.status == ReportStatus.PROCESSED || this.status == ReportStatus.REJECTED) {
            return;
        }
        this.status = ReportStatus.PROCESSED;
    }

    // 신고 반려
    public void reject() {
        if (this.status == ReportStatus.PROCESSED || this.status == ReportStatus.REJECTED) {
            return;
        }
        this.status = ReportStatus.REJECTED;
    }
}