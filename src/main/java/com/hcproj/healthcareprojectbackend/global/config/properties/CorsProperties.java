package com.hcproj.healthcareprojectbackend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS(Cross-Origin Resource Sharing) 설정을 위한 프로퍼티 클래스.
 *
 * <p>
 * <b>설정 위치</b>
 * <pre>
 * app.cors.allowed-origins:
 *   - http://localhost:3000
 *   - https://example.com
 * </pre>
 *
 * <p>
 * <b>사용 목적</b>
 * <ul>
 *   <li>환경별 허용 Origin 목록을 코드에서 분리</li>
 *   <li>SecurityConfig 또는 WebMvcConfig에서 주입 받아 사용</li>
 * </ul>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    /**
     * 허용할 Origin 목록
     */
    private List<String> allowedOrigins = new ArrayList<>();
}
