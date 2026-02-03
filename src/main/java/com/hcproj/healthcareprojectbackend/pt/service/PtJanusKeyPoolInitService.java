package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.pt.entity.PtJanusKeyStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtJanusRoomKeyEntity;
import com.hcproj.healthcareprojectbackend.pt.repository.PtJanusRoomKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Janus(WebRTC SFU) roomKey 풀을 초기화하는 서비스.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>애플리케이션 구동 시 Janus roomKey(예: 30000~39999) 풀 데이터를 DB에 준비한다.</li>
 *   <li>풀이 비어있는 경우에만 초기화를 수행하여 멱등(idempotent)하게 동작한다.</li>
 * </ul>
 *
 * <p><b>초기화 범위</b></p>
 * <ul>
 *   <li>{@code START_KEY} ~ {@code END_KEY} 범위의 키를 생성한다.</li>
 *   <li>각 키는 {@link PtJanusKeyStatus#AVAILABLE} 상태로 저장된다.</li>
 * </ul>
 *
 * <p><b>성능/안정성 고려</b></p>
 * <ul>
 *   <li>대량 INSERT를 위해 {@code BATCH_SIZE} 단위로 {@code saveAll + flush}를 수행한다.</li>
 *   <li>동시 부팅/중복 실행 등으로 인해 중복 삽입이 발생할 수 있으므로
 *       {@link org.springframework.dao.DataIntegrityViolationException}을 잡아 무시한다.</li>
 * </ul>
 *
 * <p><b>트랜잭션</b></p>
 * <ul>
 *   <li>{@link #initIfEmpty()}는 트랜잭션 내에서 수행되며,
 *       저장 후 flush를 통해 제약 위반을 조기에 감지한다.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PtJanusKeyPoolInitService {

    private final PtJanusRoomKeyRepository repo;

    /** Janus roomKey 시작 값 (포함) */
    private static final int START_KEY = 30000;
    /** Janus roomKey 끝 값 (포함) */
    private static final int END_KEY = 39999;
    /** 배치 저장 크기 */
    private static final int BATCH_SIZE = 500;

    /**
     * roomKey 풀이 비어있다면 초기화한다.
     *
     * <p>
     * 테이블에 데이터가 1개 이상 존재하면 초기화를 건너뛴다.
     * </p>
     */
    @Transactional
    public void initIfEmpty() {
        long existingCount = repo.count();
        if (existingCount > 0) {
            log.info("[PT] Janus key pool already initialized. count={}", existingCount);
            return;
        }

        log.info("[PT] Initializing Janus key pool: {}~{}", START_KEY, END_KEY);

        List<PtJanusRoomKeyEntity> buffer = new ArrayList<>(BATCH_SIZE);
        int created = 0;

        for (int key = START_KEY; key <= END_KEY; key++) {
            buffer.add(PtJanusRoomKeyEntity.builder()
                    .roomKey(key)
                    .status(PtJanusKeyStatus.AVAILABLE)
                    .build());

            if (buffer.size() >= BATCH_SIZE) {
                created += saveBatchIgnoreDuplicates(buffer);
                buffer.clear();
            }
        }

        if (!buffer.isEmpty()) {
            created += saveBatchIgnoreDuplicates(buffer);
        }

        log.info("[PT] Janus key pool initialization done. created={}", created);
    }

    /**
     * 배치 단위로 저장을 시도하되, 중복 삽입 예외는 무시한다.
     *
     * <p>
     * 동시 실행(예: 다중 인스턴스 부팅) 등으로 인해 같은 키가 이미 생성되어 있는 경우
     * 유니크/PK 제약 위반이 발생할 수 있다. 이 경우 초기화 목적상 치명적이지 않으므로 로그만 남기고 진행한다.
     * </p>
     *
     * @param batch 저장할 엔티티 배치
     * @return 저장 성공으로 간주한 개수(중복 예외 발생 시 0)
     */
    private int saveBatchIgnoreDuplicates(List<PtJanusRoomKeyEntity> batch) {
        try {
            repo.saveAll(batch);
            repo.flush();
            return batch.size();
        } catch (DataIntegrityViolationException e) {
            log.warn("[PT] Duplicate insert detected while initializing Janus key pool. Ignoring. msg={}", e.getMessage());
            return 0;
        }
    }
}
