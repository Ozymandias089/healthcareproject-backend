package com.hcproj.healthcareprojectbackend.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 엔티티 공통 시간 컬럼을 제공하는 추상 기반 클래스.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>엔티티 생성/수정 시각을 자동으로 관리한다.</li>
 *   <li>모든 도메인 엔티티에서 중복되는 시간 컬럼 정의를 제거한다.</li>
 * </ul>
 *
 * <p><b>동작 전제</b></p>
 * <ul>
 *   <li>{@link org.springframework.data.jpa.repository.config.EnableJpaAuditing}이 활성화되어 있어야 한다.</li>
 *   <li>{@link AuditingEntityListener}가 엔티티 라이프사이클을 감지한다.</li>
 * </ul>
 *
 * <p><b>설계 메모</b></p>
 * <ul>
 *   <li>Instant를 사용하여 타임존 의존성을 제거(UTC 기준 저장).</li>
 *   <li>deletedAt은 soft delete를 위한 컬럼으로,
 *       실제 삭제 대신 "삭제 시각 기록" 용도로 사용한다.</li>
 * </ul>
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    /**
     * 엔티티 생성 시각.
     *
     * <p>엔티티가 처음 persist 될 때 한 번만 자동 세팅된다.</p>
     * <ul>
     *   <li>nullable = false: 항상 값이 존재해야 한다.</li>
     *   <li>updatable = false: 이후 수정 불가.</li>
     * </ul>
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 엔티티 최종 수정 시각.
     *
     * <p>엔티티가 update 될 때마다 자동 갱신된다.</p>
     * <p>생성 직후에는 null일 수 있다.</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * 엔티티 삭제 시각(Soft Delete).
     *
     * <p>실제 DELETE 쿼리를 날리지 않고,
     * 삭제 시각을 기록하는 방식의 soft delete를 위한 컬럼이다.</p>
     *
     * <p>기본 정책:</p>
     * <ul>
     *   <li>null: 활성 상태</li>
     *   <li>값 존재: 논리적으로 삭제된 상태</li>
     * </ul>
     *
     * <p>필요 시 @Where, @SQLDelete 등을 이용해
     * soft delete 전략으로 확장할 수 있다.</p>
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * 엔티티를 논리적으로 삭제 처리한다.
     *
     * <p>실제 DELETE 쿼리 대신 deletedAt에 현재 시각을 기록한다.</p>
     * <p>하위 엔티티에서 도메인 로직의 일부로만 호출하도록 protected로 제한한다.</p>
     */
    protected void markDeleted() {
        this.deletedAt = Instant.now();
    }

    /**
     * 논리적으로 삭제된 엔 дополн 여부.
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void undoDeletion() {
        this.deletedAt = null;
    }
}
