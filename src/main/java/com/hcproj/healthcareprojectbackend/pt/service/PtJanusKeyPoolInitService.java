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

@Slf4j
@Service
@RequiredArgsConstructor
public class PtJanusKeyPoolInitService {

    private final PtJanusRoomKeyRepository repo;

    private static final int START_KEY = 30000;
    private static final int END_KEY = 39999;
    private static final int BATCH_SIZE = 500;

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
