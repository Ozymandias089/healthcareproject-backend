package com.hcproj.healthcareprojectbackend.pt.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * PT(퍼스널 트레이닝) 방 정보를 나타내는 엔티티.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>트레이너가 생성하는 라이브/예약 PT 세션의 메타데이터 저장</li>
 *   <li>상태({@link PtRoomStatus}) 및 일정/시작 시간 등을 관리</li>
 * </ul>
 *
 * <p><b>방 유형</b></p>
 * <ul>
 *   <li>{@link PtRoomType#LIVE}: 즉시 시작 가능한 라이브 세션</li>
 *   <li>{@link PtRoomType#RESERVED}: 예약 기반 세션 (scheduledStartAt 활용)</li>
 * </ul>
 *
 * <p><b>삭제 정책</b></p>
 * <ul>
 *   <li>종료/취소/강제종료 시 상태 변경 후 {@link BaseTimeEntity#markDeleted()}로 소프트 삭제 처리</li>
 *   <li>각 종료 계열 메서드는 멱등(idempotent)하게 동작한다.</li>
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "pt_rooms")
public class PtRoomEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pt_room_id")
    private Long ptRoomId;

    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private PtRoomType roomType; // LIVE | RESERVED

    @Column(name = "scheduled_start_at")
    private Instant scheduledStartAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(name = "entry_code", length = 255)
    private String entryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PtRoomStatus status; // SCHEDULED | JOINED | LEFT | KICKED

    // ---------------------------
    // Domain Actions (이름 유지)
    // ---------------------------

    public void start() {
        if (this.status == PtRoomStatus.LIVE) {
            return; // 멱등
        }
        ensureStartable();

        this.status = PtRoomStatus.LIVE;
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    public void end() {
        if (this.status == PtRoomStatus.ENDED) {
            return; // 멱등
        }
        ensureEndable();

        this.status = PtRoomStatus.ENDED;
        markDeleted();
    }

    public void cancel() {
        if (this.status == PtRoomStatus.CANCELLED) {
            return; // 멱등
        }
        ensureCancellable();

        this.status = PtRoomStatus.CANCELLED;
        markDeleted();
    }

    public void forceClose() {
        if (this.status == PtRoomStatus.FORCE_CLOSED) {
            return; // 멱등
        }
        ensureForceClosable();

        this.status = PtRoomStatus.FORCE_CLOSED;
        markDeleted();
    }


    // ---------------------------
    // Guards (전이 규칙을 도메인에)
    // ---------------------------

    private void ensureStartable() {
        // 예: 예약방/라이브방 정책이 있다면 여기서 같이 체크 가능
        // if (this.roomType == PtRoomType.RESERVED && this.scheduledStartAt == null) throw ...

        // 서비스에서 하던 "SCHEDULED만 LIVE로"를 도메인으로 이동
        if (this.status != PtRoomStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    private void ensureEndable() {
        if (this.status != PtRoomStatus.LIVE) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    private void ensureCancellable() {
        // 취소 가능 범위를 네 정책대로 정하면 됨
        // 보통: SCHEDULED만 취소 허용(이미 LIVE면 cancel 대신 end)
        if (this.status != PtRoomStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    private void ensureForceClosable() {
        // 관리자 강제 종료 정책: LIVE/ SCHEDULED 둘 다 허용 등
        // 여기서는 예시로 LIVE, SCHEDULED만 허용
        if (this.status != PtRoomStatus.LIVE && this.status != PtRoomStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}
