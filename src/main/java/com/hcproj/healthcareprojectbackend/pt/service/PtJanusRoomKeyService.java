package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.entity.PtJanusKeyStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtJanusRoomKeyEntity;
import com.hcproj.healthcareprojectbackend.pt.repository.PtJanusRoomKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PtJanusRoomKeyService {

    private final PtJanusRoomKeyRepository repo;

    @Transactional
    public PtJanusRoomKeyEntity allocate(Long ptRoomId) {
        // 1) 이미 할당된 키가 있으면 그대로 반환(멱등)
        var existing = repo.findByPtRoomId(ptRoomId);
        if (existing.isPresent()) return existing.get();

        // 2) AVAILABLE 하나를 락 잡고 가져오기
        var candidates = repo.findAvailableForUpdate(PtJanusKeyStatus.AVAILABLE, PageRequest.of(0, 1));
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.ROOM_FULL); // 키 풀이 다 찼음
        }

        PtJanusRoomKeyEntity key = candidates.getFirst();

        // 3) 할당
        key.allocateTo(ptRoomId);
        return key; // dirty checking으로 update
    }

    @Transactional
    public void releaseByPtRoomId(Long ptRoomId) {
        var keyOpt = repo.findByPtRoomId(ptRoomId);
        if (keyOpt.isEmpty()) return; // 멱등: 없으면 그냥 종료

        PtJanusRoomKeyEntity key = keyOpt.get();
        key.release();
    }
}
