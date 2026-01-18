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

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                || parameter.hasParameterAnnotation(CurrentHandle.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (parameter.hasParameterAnnotation(CurrentHandle.class)) {
            // principal=handle 이므로 getName()이 가장 깔끔
            String handle = auth.getName();
            if (handle == null || handle.isBlank()) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            return handle;
        }

        if (parameter.hasParameterAnnotation(CurrentUserId.class)) {
            Object details = auth.getDetails(); // 우리가 JwtTokenProvider에서 uid로 setDetails 했음
            if (details instanceof Number n) {
                return n.longValue();
            }
            // details가 없으면 토큰/인증 세팅이 잘못된 것
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }
}
