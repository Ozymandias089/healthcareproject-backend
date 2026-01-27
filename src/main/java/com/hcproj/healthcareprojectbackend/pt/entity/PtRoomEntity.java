package com.hcproj.healthcareprojectbackend.pt.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
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

    /**
     * PT 방을 라이브 상태로 시작 처리한다.
     *
     * <p>
     * 이미 LIVE인 경우 아무 동작도 하지 않는다(멱등성).
     * startedAt이 비어있으면 현재 시각으로 기록한다.
     * </p>
     */
    public void start() {
        if (this.status == PtRoomStatus.LIVE) {
            return;
        }
        this.status = PtRoomStatus.LIVE;
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    /**
     * PT 방을 정상 종료 처리한다.
     *
     * <p>
     * status를 ENDED로 변경하고 소프트 삭제 마킹한다.
     * 이미 ENDED인 경우 아무 동작도 하지 않는다(멱등성).
     * </p>
     */
    public void end() {
        if (this.status == PtRoomStatus.ENDED) {
            return;
        }
        this.status = PtRoomStatus.ENDED;
        markDeleted();
    }

    /**
     * PT 방을 취소 처리한다.
     *
     * <p>
     * status를 CANCELLED로 변경하고 소프트 삭제 마킹한다.
     * </p>
     */
    public void cancel() {
        if (this.status == PtRoomStatus.CANCELLED) return;
        this.status = PtRoomStatus.CANCELLED;
        markDeleted();
    }

    /**
     * 관리자에 의한 PT 방 강제 종료 처리한다.
     *
     * <p>
     * status를 FORCE_CLOSED로 변경하고 소프트 삭제 마킹한다.
     * </p>
     */
    public void forceClose() {
        if (this.status == PtRoomStatus.FORCE_CLOSED) return;
        this.status = PtRoomStatus.FORCE_CLOSED;
        markDeleted();
    }

}
