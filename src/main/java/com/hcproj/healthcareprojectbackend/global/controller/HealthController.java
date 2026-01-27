package com.hcproj.healthcareprojectbackend.global.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * 애플리케이션 상태 및 버전 정보를 제공하는 공통 헬스 체크 컨트롤러.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>서버가 정상 기동 중인지 확인하기 위한 헬스 체크 엔드포인트 제공</li>
 *   <li>현재 실행 중인 애플리케이션의 메타 정보(이름, 버전 등) 조회</li>
 * </ul>
 *
 * <p>
 * <b>사용 목적</b>
 * <ul>
 *   <li>로드밸런서 / 모니터링 시스템의 헬스 체크</li>
 *   <li>배포 후 버전 확인 및 운영 점검</li>
 * </ul>
 *
 * <p>
 * 인증이 필요 없는 공개 엔드포인트로 사용되는 것을 전제로 한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String version;

    /**
     * 애플리케이션 헬스 체크 API.
     *
     * @return 서버 상태 정보 (항상 "UP")
     */
    @GetMapping(path = "/health", produces = "application/json")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "UP"));
    }

    /**
     * 애플리케이션 버전 정보 조회 API.
     *
     * <p>
     * buildTime은 서버 응답 시점의 시간이며,
     * 실제 빌드 타임이 필요한 경우 별도의 빌드 메타데이터로 대체 가능하다.
     *
     * @return 애플리케이션 이름, 버전, 응답 시각
     */
    @GetMapping(path = "/version", produces = "application/json")
    public ApiResponse<Map<String, Object>> version() {
        return ApiResponse.ok(Map.of(
                "name", appName,
                "version", version,
                "buildTime", Instant.now().toString()
        ));
    }
}
