package com.hcproj.healthcareprojectbackend.global.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 관리자(ADMIN) 권한이 있는 사용자만 접근 가능하도록 제한하는 보안 어노테이션.
 *
 * <p>
 * 이 어노테이션은 Spring Security의 메서드 보안(@PreAuthorize)을 기반으로 하며,
 * 현재 {@link org.springframework.security.core.context.SecurityContext}에 저장된
 * {@link org.springframework.security.core.Authentication}의 권한(Authority)에
 * {@code ROLE_ADMIN}이 포함되어 있는 경우에만 대상 메서드 또는 클래스의 실행을 허용한다.
 * </p>
 *
 * <h3>동작 원리</h3>
 * <ol>
 *   <li>요청이 {@code JwtAuthenticationFilter}를 통과하면서 JWT가 검증된다.</li>
 *   <li>JWT의 {@code role} 클레임이 {@code ROLE_{role}} 형태의
 *       {@link org.springframework.security.core.GrantedAuthority}로 변환된다.</li>
 *   <li>Spring Security AOP가 메서드 호출 전에
 *       {@code hasRole('ADMIN')} 표현식을 평가한다.</li>
 *   <li>권한이 없으면 {@code AccessDeniedException}이 발생하고,
 *       {@link com.hcproj.healthcareprojectbackend.global.security.RestAccessDeniedHandler}
 *       에 의해 403(FORBIDDEN) 응답이 반환된다.</li>
 * </ol>
 *
 * <h3>적용 위치</h3>
 * <ul>
 *   <li><b>컨트롤러 메서드</b>: 특정 API를 관리자 전용으로 제한</li>
 *   <li><b>컨트롤러 클래스</b>: 클래스 내 모든 엔드포인트를 관리자 전용으로 제한</li>
 *   <li><b>서비스 메서드</b>: 외부 호출 경로가 여러 개인 핵심 비즈니스 로직 보호</li>
 * </ul>
 *
 * <h3>사용 예시</h3>
 *
 * <pre>{@code
 * @AdminOnly
 * @PostMapping("/api/admin/foods")
 * public ApiResponse<Void> createFood(@RequestBody FoodCreateRequest request) {
 *     foodService.create(request);
 *     return ApiResponse.ok();
 * }
 * }</pre>
 *
 * <pre>{@code
 * @AdminOnly
 * @Service
 * public class AdminFoodService {
 *
 *     public void deleteFood(Long foodId) {
 *         // 관리자만 접근 가능
 *     }
 * }
 * }</pre>
 *
 * <h3>전제 조건</h3>
 * <ul>
 *   <li>{@code @EnableMethodSecurity}가 활성화되어 있어야 한다.</li>
 *   <li>JWT 인증 과정에서 {@code role} 클레임이
 *       {@code ROLE_ADMIN} 형태의 Authority로 변환되어야 한다.</li>
 *   <li>SecurityConfig 또는 Filter에서 {@link Authentication}이
 *       {@link SecurityContextHolder}에 정상적으로 설정되어야 한다.</li>
 * </ul>
 *
 * <h3>주의 사항</h3>
 * <ul>
 *   <li>이 어노테이션은 <b>인가(Authorization)</b>만 담당하며,
 *       인증(Authentication)이 선행되지 않은 요청은 자동으로 차단된다.</li>
 *   <li>{@code permitAll()}로 열려 있는 엔드포인트에 적용하더라도
 *       메서드 호출 시점에는 권한 검증이 수행된다.</li>
 *   <li>단위 테스트에서 이 어노테이션이 붙은 메서드를 호출할 경우
 *       WithMockUser
 *       또는 SecurityContext 설정이 필요하다.</li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
public @interface AdminOnly {}

