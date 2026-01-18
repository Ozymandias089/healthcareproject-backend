package com.hcproj.healthcareprojectbackend.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-001", "요청값이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-404", "대상을 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류가 발생했습니다."),

    // Auth/Security
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-002", "권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-003", "토큰이 유효하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-004", "토큰이 만료되었습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-005", "이메일 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "AUTH-006", "이미 사용 중인 이메일입니다."),
    HANDLE_DUPLICATED(HttpStatus.CONFLICT, "AUTH-007", "이미 사용 중인 핸들입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }
    public String message() { return message; }
}
