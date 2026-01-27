package com.hcproj.healthcareprojectbackend.pt.bootstrap;

import com.hcproj.healthcareprojectbackend.pt.service.PtJanusKeyPoolInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Janus roomKey 풀 초기화를 위한 부트스트랩 컴포넌트.
 *
 * <p>
 * 애플리케이션 구동 시 {@link PtJanusKeyPoolInitService#initIfEmpty()}를 호출하여
 * roomKey 풀이 비어있다면 초기 데이터를 채운다.
 * </p>
 *
 * <p><b>주의</b></p>
 * <ul>
 *   <li>CommandLineRunner는 애플리케이션 시작 시점에 실행된다.</li>
 *   <li>초기화 로직은 멱등(idempotent)해야 하며, 중복 데이터 생성이 없어야 한다.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class PtJanusKeyPoolInitializer implements CommandLineRunner {

    private final PtJanusKeyPoolInitService initService;

    @Override
    public void run(String... args) {
        initService.initIfEmpty();
    }
}
