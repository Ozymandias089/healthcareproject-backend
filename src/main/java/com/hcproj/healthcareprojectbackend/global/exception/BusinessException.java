package com.hcproj.healthcareprojectbackend.global.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 의도적으로 발생시키는 예외.
 *
 * <p>컨트롤러/서비스 레이어에서
 * "정상적인 오류 상황"을 표현하기 위해 사용한다.</p>
 *
 * <p>예시:</p>
 * <ul>
 *   <li>이메일 중복</li>
 *   <li>로그인 실패</li>
 *   <li>토큰 만료/위변조</li>
 * </ul>
 *
 * <p>이 예외는 {@link GlobalExceptionHandler}에서
 * 공통적으로 처리되어 ApiResponse.fail 형태로 변환된다.</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 이 예외에 대응하는 에러 코드 */
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        // RuntimeException 메시지에도 ErrorCode 메시지를 그대로 사용
        super(errorCode.message());
        this.errorCode = errorCode;
    }
}