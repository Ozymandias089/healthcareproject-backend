package com.hcproj.healthcareprojectbackend.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 프로젝트 공통 에러 코드 정의.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>HTTP Status(상태 코드)와 내부 에러 코드(code), 사용자 메시지(message)를 한 곳에서 통제한다.</li>
 *   <li>{@link com.hcproj.healthcareprojectbackend.global.exception.BusinessException}은 ErrorCode를 들고 다닌다.</li>
 *   <li>{@link com.hcproj.healthcareprojectbackend.global.exception.GlobalExceptionHandler}에서 ApiResponse.fail로 변환한다.</li>
 * </ul>
 *
 * <p><b>에러 코드 규칙</b></p>
 * <ul>
 *   <li>COMMON-xxx: 인증/도메인과 무관한 공통 오류</li>
 *   <li>AUTH-xxx: 인증/인가/토큰 등 Security 관련 오류</li>
 *   <li>AI-xxx: AI/외부 의존성 관련 오류</li>
 *   <li>USER-xxx / COMMUNITY-xxx / PT-xxx / EXERCISE-xxx / FOOD-xxx / WORKOUT-xxx / DIET-xxx / TRAINER-xxx / ADMIN-xxx / UPLOAD-xxx</li>
 * </ul>
 *
 * <p><b>주의</b></p>
 * <ul>
 *   <li>클라이언트는 message보다 code를 기준으로 분기하는 것이 안전하다</li>
 * </ul>
 */
public enum ErrorCode {

    // -------------------------
    // Common
    // -------------------------

    /** 요청 값 검증 실패(Validation) / 잘못된 파라미터 등 */
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-001", "요청값이 올바르지 않습니다"),

    /** 리소스가 존재하지 않을 때 (범용) */
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-404", "대상을 찾을 수 없습니다"),

    /** 처리되지 않은 예외 등 서버 내부 오류 (대표) */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류가 발생했습니다"),

    /** (선택) 기존 코드 호환/구분용: INTERNAL_ERROR와 의미 유사 */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-501", "서버 오류가 발생했습니다"),

    /** 데이터가 유효하지 않음(서버가 처리할 수 없는 상태) */
    INVALID_DATA(HttpStatus.UNPROCESSABLE_ENTITY, "COMMON-002", "데이터가 유효하지 않습니다"),

    /** 데이터 중복 (이미 신청함 등) */
    ALREADY_EXISTS(HttpStatus.CONFLICT, "COMMON-409", "이미 존재하는 데이터입니다"),

    /** 유효하지 않은 상태 변경 요청 */
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "COMMON-410", "유효하지 않은 상태 변경 요청입니다"),

    // -------------------------
    // AI
    // -------------------------

    /** AI 응답 JSON 파싱 실패 */
    AI_JSON_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "AI-001", "AI 응답 처리에 실패했습니다"),

    /** AI 화이트리스트(foods) 구성 실패 (내부 로직/직렬화/데이터 문제) */
    AI_ALLOWED_FOODS_BUILD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI-002", "AI 생성 준비에 실패했습니다"),

    /** AI 산출물(스키마/규칙 위반 등) 유효하지 않음 */
    AI_INVALID_OUTPUT(HttpStatus.BAD_GATEWAY, "AI-003", "AI 응답이 유효하지 않습니다"),

    /** AI 화이트리스트(exercises) 구성 실패 */
    AI_ALLOWED_EXERCISES_BUILD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI-004", "AI 생성 준비에 실패했습니다"),

    // -------------------------
    // Auth/Security
    // -------------------------

    /** 인증 필요(로그인 필요) - 토큰이 없거나 SecurityContext가 비어있음 */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증이 필요합니다"),

    /** 권한 부족 - 인증은 됐지만 ROLE이 부족함 */
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-002", "권한이 없습니다"),

    /** 토큰이 유효하지 않음(위변조/형식 오류/폐기됨/Redis whitelist 미존재 등) */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-003", "토큰이 유효하지 않습니다"),

    /** 토큰 만료(Access/Refresh 모두 공통으로 사용) */
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-004", "토큰이 만료되었습니다"),

    /** 로그인 실패(보안상 계정 존재 여부를 숨기기 위해 단일 메시지로 통일) */
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-005", "이메일 또는 비밀번호가 올바르지 않습니다"),

    /** 이메일 중복 */
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "AUTH-006", "이미 사용 중인 이메일입니다"),

    /** 핸들 중복 */
    HANDLE_DUPLICATED(HttpStatus.CONFLICT, "AUTH-007", "이미 사용 중인 핸들입니다"),

    /** 동일한 요청 과다 */
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH-008", "동일한 요청을 너무 많이 생성했습니다"),

    /** 이미 탈퇴된 회원 */
    ALREADY_WITHDRAWN(HttpStatus.CONFLICT, "AUTH-015", "이미 탈퇴된 회원입니다"),

    /** 입력값이 올바르지 않음(인증/보안 흐름에서 사용) */
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "AUTH-016", "입력값이 올바르지 않습니다"),

    /** 비밀번호가 일치하지 않음 */
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH-017", "비밀번호가 일치하지 않습니다"),

    SOCIAL_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH-010", "소셜 로그인에 이메일이 필요합니다"),
    SOCIAL_ACCOUNT_TAKEN(HttpStatus.CONFLICT, "AUTH-011", "이미 연동된 프로바이더입니다"),
    SOCIAL_ALREADY_CONNECTED(HttpStatus.CONFLICT, "AUTH-012", "이미 해당 프로바이더를 연동했습니다"),
    SOCIAL_ACCOUNT_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "AUTH-013", "해당 프로바이더가 연결되어있지 않습니다"),
    CANNOT_DISCONNECT_LAST_LOGIN_METHOD(HttpStatus.CONFLICT, "AUTH-014", "마지막 로그인 수단입니다"),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "AUTH-015", "이용이 정지된 계정입니다."),

    // -------------------------
    // User
    // -------------------------

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다"),
    INVALID_INJURY_LEVEL(HttpStatus.BAD_REQUEST, "USER-002", "부상 레벨이 존재하지 않습니다"),
    INVALID_ALLERGY_TYPE(HttpStatus.BAD_REQUEST, "USER-003", "알러지 타입이 존재하지 않습니다"),

    // -------------------------
    // Community (Post/Comment/Report)
    // -------------------------

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY-001", "게시글을 찾을 수 없습니다"),
    NOT_POST_AUTHOR(HttpStatus.FORBIDDEN, "COMMUNITY-002", "게시글 수정 또는 삭제 권한이 없습니다"),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMUNITY-003", "댓글을 찾을 수 없습니다"),
    NOT_COMMENT_AUTHOR(HttpStatus.FORBIDDEN, "COMMUNITY-004", "댓글 작성자만 수정 또는 삭제할 수 있습니다"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMUNITY-005", "잘못된 입력값입니다"),

    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMUNITY-006", "본인의 게시글 또는 댓글은 신고할 수 없습니다"),
    NOTICE_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMUNITY-007", "공지사항은 신고할 수 없습니다"),
    ALREADY_REPORTED(HttpStatus.CONFLICT, "COMMUNITY-008", "이미 신고한 게시글 또는 댓글입니다"),

    // -------------------------
    // PT
    // -------------------------

    /** 예약이 불가능한 상태입니다 (방 종료/취소 등) */
    RESERVATION_NOT_ALLOWED(HttpStatus.CONFLICT, "PT-001", "예약이 불가능한 상태입니다"),

    ROOM_FULL(HttpStatus.CONFLICT, "PT-002", "방의 정원이 초과되었습니다"),
    ALREADY_RESERVED(HttpStatus.CONFLICT, "PT-003", "이미 예약된 상태입니다"),
    INVALID_ENTRY_CODE(HttpStatus.FORBIDDEN, "PT-004", "참여 코드가 일치하지 않습니다"),
    CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "PT-005", "진행 중이거나 종료된 방의 예약은 취소할 수 없습니다"),
    NOT_JOINED(HttpStatus.CONFLICT, "PT-006", "해당 사용자는 현재 방에 참여 중이 아닙니다"),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "PT-007", "존재하지 않는 방입니다."),

    // -------------------------
    // Exercise
    // -------------------------

    EXERCISE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXERCISE-001", "운동을 찾을 수 없습니다"),

    // -------------------------
    // Food
    // -------------------------

    FOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "FOOD-001", "음식을 찾을 수 없습니다"),

    // -------------------------
    // Workout
    // -------------------------

    WORKOUT_DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKOUT-001", "해당 날짜의 운동 계획이 없습니다"),
    WORKOUT_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKOUT-002", "운동 항목을 찾을 수 없습니다"),

    // -------------------------
    // Diet
    // -------------------------

    DIET_DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIET-001", "해당 날짜의 식단 기록이 없습니다"),
    DIET_MEAL_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "DIET-002", "식단 항목을 찾을 수 없습니다"),

    // -------------------------
    // Trainer
    // -------------------------

    NOT_TRAINER(HttpStatus.FORBIDDEN, "TRAINER-001", "트레이너 권한이 필요합니다"),

    // -------------------------
    // Admin
    // -------------------------

    USER_ALREADY_BANNED(HttpStatus.CONFLICT, "ADMIN-001", "이미 차단된 회원입니다"),
    USER_NOT_BANNED(HttpStatus.CONFLICT, "ADMIN-002", "차단되지 않은 회원입니다"),
    CANNOT_BAN_ADMIN(HttpStatus.FORBIDDEN, "ADMIN-003", "관리자는 차단할 수 없습니다"),
    TRAINER_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN-004", "트레이너 신청 내역이 없습니다"),
    TRAINER_REJECT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "ADMIN-005", "거절 사유를 입력해주세요"),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN-006", "신고 내역을 찾을 수 없습니다"),

    // -------------------------
    // Upload
    // -------------------------

    INVALID_UPLOAD_TYPE(HttpStatus.BAD_REQUEST, "UPLOAD-001", "지원하지 않는 업로드 타입입니다"),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "UPLOAD-002", "지원하지 않는 파일 형식입니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "UPLOAD-003", "파일 크기가 10MB를 초과합니다"),
    PRESIGNED_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD-004", "업로드 URL 생성에 실패했습니다");

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
