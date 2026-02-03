package com.hcproj.healthcareprojectbackend.pt.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Janus(WebRTC SFU) 방 키(roomKey) 풀을 관리하기 위한 엔티티.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>Janus에서 사용할 roomKey(예: 30000~39999)를 미리 확보/관리한다.</li>
 *   <li>PT 방({@code ptRoomId})에 roomKey를 할당/해제하는 상태를 기록한다.</li>
 * </ul>
 *
 * <p><b>DB 제약</b></p>
 * <ul>
 *   <li>{@code uk_pt_janus_keys_pt_room}: (pt_room_id) 유니크
 *       → 하나의 PT 방은 하나의 roomKey만 할당 가능</li>
 *   <li>{@code idx_pt_janus_keys_status}: status 인덱스
 *       → AVAILABLE 키 조회 최적화</li>
 * </ul>
 *
 * <p><b>상태/일관성 규칙</b></p>
 * <ul>
 *   <li>AVAILABLE 상태에서는 {@code ptRoomId == null}이어야 한다.</li>
 *   <li>ALLOCATED 상태에서는 {@code ptRoomId != null}을 기대한다.</li>
 *   <li>{@link #isAvailable()}는 상태와 ptRoomId를 함께 검사한다.</li>
 * </ul>
 *
 * <p>
 * 실제 동시성 제어(동시 할당 경쟁)는 서비스/스토어 레이어에서 트랜잭션/락으로 보장한다.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "pt_janus_room_keys",
        indexes = {
                @Index(name = "idx_pt_janus_keys_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pt_janus_keys_pt_room", columnNames = {"pt_room_id"})
        }
)
public class PtJanusRoomKeyEntity extends BaseTimeEntity {

    @Id
    @Column(name = "room_key")
    private Integer roomKey; // 30000~39999

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PtJanusKeyStatus status;

    @Column(name = "pt_room_id")
    private Long ptRoomId; // allocated room id (nullable, unique)

    @Column(name = "allocated_at")
    private Instant allocatedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    /**
     * 현재 키가 할당 가능한지 확인한다.
     *
     * @return AVAILABLE 상태이며 아직 어떤 PT 방에도 할당되지 않았으면 true
     */
    public boolean isAvailable() {
        return this.status == PtJanusKeyStatus.AVAILABLE && this.ptRoomId == null;
    }

    /**
     * roomKey를 특정 PT 방에 할당 처리한다.
     *
     * <p>
     * allocatedAt을 현재 시각으로 기록하며, releasedAt은 초기화된다.
     * </p>
     *
     * @param ptRoomId 할당 대상 PT 방 ID
     */
    public void allocateTo(Long ptRoomId) {
        this.status = PtJanusKeyStatus.ALLOCATED;
        this.ptRoomId = ptRoomId;
        this.allocatedAt = Instant.now();
        this.releasedAt = null;
    }

    /**
     * roomKey 할당을 해제하여 다시 사용 가능 상태로 되돌린다.
     *
     * <p>
     * releasedAt을 현재 시각으로 기록하며, allocatedAt/ptRoomId는 초기화된다.
     * </p>
     */
    public void release() {
        this.status = PtJanusKeyStatus.AVAILABLE;
        this.ptRoomId = null;
        this.releasedAt = Instant.now();
        this.allocatedAt = null;
    }
}
