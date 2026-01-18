package com.hcproj.healthcareprojectbackend.global.security.annotation;

import java.lang.annotation.*;

/**
 * 현재 인증된 사용자의 {@code handle}를 컨트롤러 메서드 파라미터로 주입한다.
 *
 * <p>
 * 이 어노테이션은 JWT 인증이 완료된 요청에서만 사용 가능하며,
 * JWT의 {@code subject(sub)} 값으로 설정된 사용자 handle을 주입한다.
 * </p>
 *
 * <h3>전제 조건</h3>
 * <ul>
 *   <li>JWT 토큰이 유효해야 한다.</li>
 *   <li>JWT 생성 시 {@code subject}에 handle이 설정되어 있어야 한다.</li>
 *   <li>Authentication의 {@code principal}이 handle(String)이어야 한다.</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * @GetMapping("/profile")
 * public ApiResponse<ProfileResponse> profile(@CurrentHandle String handle) {
 *     return ApiResponse.ok(profileService.getByHandle(handle));
 * }
 * }</pre>
 *
 * <h3>권장 사용 사례</h3>
 * <ul>
 *   <li>프로필 조회</li>
 *   <li>닉네임/핸들 기반 리소스 접근</li>
 *   <li>로그/감사용 사용자 식별</li>
 * </ul>
 *
 * <h3>주의 사항</h3>
 * <ul>
 *   <li>{@code permitAll} 엔드포인트에서 사용하면 {@code UNAUTHORIZED} 예외가 발생한다.</li>
 *   <li>handle은 서버에서 자동 생성되며, 변경되지 않는 값임을 전제로 한다.</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentHandle {}
