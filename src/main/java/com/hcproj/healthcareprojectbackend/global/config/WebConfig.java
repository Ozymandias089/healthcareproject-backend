package com.hcproj.healthcareprojectbackend.global.config;

import com.hcproj.healthcareprojectbackend.global.security.resolver.CurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 관련 설정.
 *
 * <p><b>현재 역할</b></p>
 * <ul>
 *   <li>{@link CurrentUserArgumentResolver}를 MVC ArgumentResolver 체인에 등록한다.</li>
 * </ul>
 *
 * <p>이 설정을 통해 컨트롤러에서 @CurrentUserId, @CurrentHandle을 사용 가능해진다.</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    /**
     * 커스텀 ArgumentResolver 등록.
     *
     * <p>Spring은 기본 Resolver 목록이 있고, 여기에 커스텀 Resolver를 추가한다.</p>
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
