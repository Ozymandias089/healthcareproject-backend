package com.hcproj.healthcareprojectbackend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * API 공통 응답 래퍼.
 *
 * <p>모든 API는 성공/실패 여부와 관계없이 동일한 JSON 구조를 반환한다.</p>
 *
 * <pre>
 * 성공:
 * {
 *   "success": true,
 *   "data": { ... }
 * }
 *
 * 실패:
 * {
 *   "success": false,
 *   "error": {
 *     "code": "AUTH-003",
 *     "message": "토큰이 유효하지 않습니다."
 *   }
 * }
 * </pre>
 *
 * <p><b>설계 원칙</b></p>
 * <ul>
 *   <li>HTTP Status는 의미에 맞게 사용한다(401, 403, 400, 500 등).</li>
 *   <li>클라이언트는 success 플래그와 error.code를 기준으로 분기한다.</li>
 *   <li>data가 없는 경우 JSON에서 완전히 제거된다(null 필드 제거).</li>
 * </ul>
 *
 * @param <T> 성공 시 반환되는 data의 타입
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error
) {

    /**
     * 성공 응답(data 포함).
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 성공 응답(data 없음).
     *
     * <p>예: 로그아웃, 삭제 API 등</p>
     */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    /**
     * 실패 응답.
     *
     * @param code 에러 코드(ErrorCode.code)
     * @param message 사용자에게 노출할 메시지
     */
    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message));
    }

    /**
     * 에러 정보 객체.
     *
     * <p>HTTP Status와는 별도로, 클라이언트가 분기 처리할 수 있는
     * 비즈니스 에러 코드를 제공한다.</p>
     */
    public record ApiError(String code, String message) {}

    /**
     * 생성 성공 응답 (201 Created).
     *
     * <p>데이터 생성 성공 시 사용</p>
     */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data, null);
    }
}
