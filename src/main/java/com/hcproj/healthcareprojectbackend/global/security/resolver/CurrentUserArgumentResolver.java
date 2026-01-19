package com.hcproj.healthcareprojectbackend.global.security.resolver;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentHandle;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 컨트롤러 메서드 파라미터에서 현재 로그인 사용자의 정보를 주입하는 ArgumentResolver.
 *
 * <p>사용 예:</p>
 * <pre>
 * {@code
 * @GetMapping("/me")
 * public ApiResponse<MyDto> me(@CurrentUserId Long userId, @CurrentHandle String handle) { ... }
 * }
 * </pre>
 *
 * <p><b>전제 조건</b></p>
 * <ul>
 *   <li>{@link com.hcproj.healthcareprojectbackend.global.security.jwt.JwtAuthenticationFilter}
 *       가 Access Token을 검증하고 SecurityContext에 Authentication을 세팅해야 한다.</li>
 *   <li>{@link com.hcproj.healthcareprojectbackend.global.security.jwt.JwtTokenProvider#getAuthentication(String)}
 *       에서 principal=handle, details=uid 로 설정되어 있어야 한다.</li>
 * </ul>
 *
 * <p><b>매핑 규칙</b></p>
 * <ul>
 *   <li>{@link CurrentHandle}: Authentication#getName() (= principal handle)</li>
 *   <li>{@link CurrentUserId}: Authentication#getDetails() (= uid)</li>
 * </ul>
 *
 * <p><b>에러 처리</b></p>
 * <ul>
 *   <li>인증 정보가 없거나 유효하지 않으면 {@link ErrorCode#UNAUTHORIZED}를 발생시킨다.</li>
 *   <li>설계상 도달하면 안 되는 분기(애노테이션 누락 등)는 {@link ErrorCode#INTERNAL_ERROR}로 처리한다.</li>
 * </ul>
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 현재 Resolver가 처리할 파라미터인지 여부를 판단한다.
     *
     * <p>@CurrentUserId 또는 @CurrentHandle이 붙은 파라미터면 처리 대상이다.</p>
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                || parameter.hasParameterAnnotation(CurrentHandle.class);
    }

    /**
     * 실제 파라미터 값을 만들어 컨트롤러에 주입한다.
     *
     * <p>SecurityContext에서 Authentication을 가져와
     * 필요한 값(handle/userId)을 추출한다.</p>
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        // SecurityContext는 JwtAuthenticationFilter가 세팅해준다.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체가 없거나 인증되지 않은 경우(비로그인/토큰 없음/토큰 실패 등)
        // - 이 경우는 "인증이 필요"하므로 UNAUTHORIZED로 처리
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 1) @CurrentHandle: principal(handle)을 반환
        if (parameter.hasParameterAnnotation(CurrentHandle.class)) {
            // principal=handle 이므로 getName()이 가장 깔끔
            String handle = auth.getName();
            if (handle == null || handle.isBlank()) {
                // 인증 객체는 있지만 principal이 비어있다면 토큰/인증 세팅이 잘못된 상태
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            return handle;
        }

        // 2) @CurrentUserId: details(uid)을 반환
        if (parameter.hasParameterAnnotation(CurrentUserId.class)) {
            // JwtTokenProvider에서 auth.setDetails(uid)로 넣어둔 값
            Object details = auth.getDetails();

            if (details instanceof Number n) {
                return n.longValue();
            }

            // uid를 details에 넣는 정책인데 없다면:
            // - 토큰에 uid claim이 빠졌거나
            // - JwtTokenProvider.getAuthentication() 구현이 바뀌었거나
            // - 다른 Authentication 구현체가 들어온 경우
            //
            // 외부 사용자 관점에서는 "인증 실패"로 보는 것이 자연스러워 UNAUTHORIZED 처리
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // supportsParameter에서 걸렀으므로 여기 도달하면 안 됨(방어 코드)
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }
}
