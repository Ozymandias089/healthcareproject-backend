package com.hcproj.healthcareprojectbackend.pt.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "pt_reservations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pt_res_room_user", columnNames = {"pt_room_id", "user_id"})
})
public class PtReservationEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pt_reservation_id")
    private Long ptReservationId;

    @Column(name = "pt_room_id", nullable = false)
    private Long ptRoomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false, length = 20)
    private PtReservationStatus status; // REQUESTED | CANCELLED

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    // 취소된 예약을 다시 활성화 (재예약)
    public void recover() {
        this.status = PtReservationStatus.REQUESTED;
        this.cancelledAt = null;
    }

    public void cancel() {
        this.status = PtReservationStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }
}
