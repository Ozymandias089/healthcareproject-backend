package com.hcproj.healthcareprojectbackend.global.security.annotation;

import java.lang.annotation.*;

/**
 * 현재 인증된 사용자의 {@code userId}를 컨트롤러 메서드 파라미터로 주입한다.
 *
 * <p>
 * 이 어노테이션은 JWT 인증이 완료된 요청에서만 사용 가능하며,
 * {@link org.springframework.security.core.Authentication#getDetails()} 에 저장된
 * 사용자 ID(uid)를 {@link Long} 타입으로 주입한다.
 * </p>
 *
 * <h3>전제 조건</h3>
 * <ul>
 *   <li>JWT 토큰이 유효해야 한다.</li>
 *   <li>JWT 생성 시 {@code uid} 클레임이 포함되어 있어야 한다.</li>
 *   <li>{@code JwtTokenProvider}에서 Authentication의 {@code details}에 uid를 설정해야 한다.</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * @DeleteMapping("/me")
 * public ApiResponse<Void> withdraw(@CurrentUserId Long userId) {
 *     userService.withdraw(userId);
 *     return ApiResponse.ok();
 * }
 * }</pre>
 *
 * <h3>주의 사항</h3>
 * <ul>
 *   <li>{@code permitAll} 엔드포인트에서 사용하면 {@code UNAUTHORIZED} 예외가 발생한다.</li>
 *   <li>비인증 요청 또는 토큰이 없는 경우 {@code UNAUTHORIZED} 예외가 발생한다.</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUserId {}
