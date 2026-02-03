package com.hcproj.healthcareprojectbackend.pt.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * PT 방 참가자 정보를 나타내는 엔티티.
 *
 * <p><b>모델링 규칙</b></p>
 * <ul>
 *   <li>한 사용자(userId)는 같은 PT 방(ptRoomId)에 최대 1개의 참가 레코드만 가진다.</li>
 * </ul>
 *
 * <p><b>DB 제약</b></p>
 * <ul>
 *   <li>{@code uk_pt_room_participant_room_user}: (pt_room_id, user_id) 유니크</li>
 *   <li>{@code idx_pt_room_participant_user}: user_id 인덱스</li>
 *   <li>{@code idx_pt_room_participant_room_status}: (pt_room_id, status) 인덱스</li>
 * </ul>
 *
 * <p><b>상태 전이</b></p>
 * <ul>
 *   <li>JOINED → LEFT/KICKED (퇴장 시각 기록)</li>
 *   <li>재입장 시 {@link #join()}에서 leftAt을 null로 초기화</li>
 *   <li>각 메서드는 멱등(idempotent)하게 동작한다.</li>
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "pt_room_participants", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pt_room_participant_room_user", columnNames = {"pt_room_id", "user_id"})
}, indexes = {
        @Index(name = "idx_pt_room_participant_user", columnList = "user_id"),
        @Index(name = "idx_pt_room_participant_room_status", columnList = "pt_room_id,status")
})
public class PtRoomParticipantEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pt_room_participant_id")
    private Long ptRoomParticipantId;

    @Column(name = "pt_room_id", nullable = false)
    private Long ptRoomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PtParticipantStatus status; // JOINED | LEFT | KICKED

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    /**
     * 참가자의 방 입장을 처리한다.
     *
     * <p>
     * 이미 JOINED이면 아무 동작도 하지 않는다(멱등성).
     * 재입장 시 leftAt을 초기화한다.
     * </p>
     */
    public void join() {
        if (this.status == PtParticipantStatus.JOINED) return;

        this.status = PtParticipantStatus.JOINED;
        this.joinedAt = Instant.now();
        this.leftAt = null; // 재입장일 경우 퇴장 시간 초기화
    }

    /**
     * 참가자의 방 퇴장을 처리한다.
     *
     * <p>
     * 이미 LEFT이거나 CANCELLED이면 무시한다(멱등성).
     * </p>
     */
    public void exit() {
        if (this.status == PtParticipantStatus.LEFT || this.status == PtParticipantStatus.CANCELLED) return;

        this.status = PtParticipantStatus.LEFT;
        this.leftAt = Instant.now(); // 퇴장 시간 기록
    }

    /**
     * 참가자를 강퇴 처리한다.
     *
     * <p>
     * 이미 KICKED이거나 LEFT이면 무시한다(멱등성).
     * </p>
     */
    public void kick() {
        if (this.status == PtParticipantStatus.KICKED || this.status == PtParticipantStatus.LEFT) return;

        this.status = PtParticipantStatus.KICKED;
        this.leftAt = Instant.now();
    }
}
