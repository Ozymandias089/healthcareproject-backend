package com.hcproj.healthcareprojectbackend.global.exception;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기.
 *
 * <p>컨트롤러에서 발생한 예외를 가로채
 * HTTP Status + ApiResponse 표준 형식으로 변환한다.</p>
 *
 * <p><b>예외 분류 전략</b></p>
 * <ul>
 *   <li>{@link BusinessException}: 비즈니스 오류(의도된 실패)</li>
 *   <li>Validation 예외: 잘못된 요청 값</li>
 *   <li>기타 Exception: 서버 내부 오류</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리.
     *
     * <p>서비스/도메인 레이어에서 발생시킨 BusinessException을
     * ErrorCode에 정의된 HTTP Status와 메시지로 그대로 반환한다.</p>
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        var ec = e.getErrorCode();
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.fail(ec.code(), ec.message()));
    }

    /**
     * @Valid DTO 검증 실패 처리.
     *
     * <p>요청 값 형식이 잘못된 경우(필수값 누락, 길이 제한 위반 등)</p>
     *
     * <p>세부 필드 에러를 내려줄 수도 있지만,
     * 현재는 단순화하여 INVALID_REQUEST로 통일한다.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        var ec = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.fail(ec.code(), ec.message()));
    }

    /**
     * 파라미터 레벨 제약(@Validated) 검증 실패 처리.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException e) {
        var ec = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.fail(ec.code(), ec.message()));
    }

    /**
     * 처리되지 않은 모든 예외의 최종 fallback.
     *
     * <p>실제 원인은 로그로 남기고,
     * 클라이언트에는 내부 구현을 노출하지 않는다.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
        var ec = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.fail(ec.code(), ec.message()));
    }

}
