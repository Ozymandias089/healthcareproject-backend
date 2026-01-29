# Error Codes

## Common

| Code       | Message                     | Status                    | Description          |
|------------|-----------------------------|---------------------------|----------------------|
| COMMON-001 | 요청값이 올바르지 않습니다        | 400 Bad Request           | 검증 실패/잘못된 파라미터 |
| COMMON-002 | 데이터가 유효하지 않습니다        | 422 Unprocessable Entity  | 유효성 실패           |
| COMMON-404 | 대상을 찾을 수 없습니다         | 404 Not Found             | 리소스 없음           |
| COMMON-409 | 이미 존재하는 데이터입니다        | 409 Conflict              | 중복 데이터           |
| COMMON-410 | 유효하지 않은 상태 변경 요청입니다   | 409 Conflict              | 상태 전이 실패         |
| COMMON-500 | 서버 오류가 발생했습니다         | 500 Internal Server Error | 미처리 예외           |
| COMMON-501 | 서버 오류가 발생했습니다         | 500 Internal Server Error | 내부 오류(호환용)       |

## AI

| Code   | Message                 | Status                    | Description                |
|--------|-------------------------|---------------------------|----------------------------|
| AI-001 | AI 응답 처리에 실패했습니다 | 502 Bad Gateway           | JSON 파싱 실패             |
| AI-002 | AI 생성 준비에 실패했습니다 | 500 Internal Server Error | foods 화이트리스트 구성 실패 |
| AI-003 | AI 응답이 유효하지 않습니다 | 502 Bad Gateway           | 스키마/규칙 위반            |
| AI-004 | AI 생성 준비에 실패했습니다 | 500 Internal Server Error | exercises 화이트리스트 구성 실패 |

## Auth/Security

| Code     | Message                          | Status                | Description        |
|----------|----------------------------------|-----------------------|--------------------|
| AUTH-001 | 인증이 필요합니다                    | 401 Unauthorized      | 로그인 필요         |
| AUTH-002 | 권한이 없습니다                     | 403 Forbidden         | 권한 부족          |
| AUTH-003 | 토큰이 유효하지 않습니다               | 401 Unauthorized      | 위변조/형식 오류/폐기 |
| AUTH-004 | 토큰이 만료되었습니다                  | 401 Unauthorized      | 만료 토큰          |
| AUTH-005 | 이메일 또는 비밀번호가 올바르지 않습니다  | 401 Unauthorized      | 로그인 실패         |
| AUTH-006 | 이미 사용 중인 이메일입니다              | 409 Conflict          | 이메일 중복         |
| AUTH-007 | 이미 사용 중인 핸들입니다               | 409 Conflict          | 핸들 중복          |
| AUTH-008 | 동일한 요청을 너무 많이 생성했습니다       | 429 Too Many Requests | 요청 과다          |
| AUTH-009 | 이용이 정지된 계정입니다.               | 403 Forbidden         | 이용 정지          |
| AUTH-010 | 소셜 로그인에 이메일이 필요합니다         | 400 Bad Request       | 소셜 이메일 필수      |
| AUTH-011 | 이미 연동된 프로바이더입니다             | 409 Conflict          | 소셜 연동 중복       |
| AUTH-012 | 이미 해당 프로바이더를 연동했습니다        | 409 Conflict          | 소셜 연동 중복       |
| AUTH-013 | 해당 프로바이더가 연결되어있지 않습니다     | 400 Bad Request       | 소셜 미연결         |
| AUTH-014 | 마지막 로그인 수단입니다                | 409 Conflict          | 연결 해제 불가       |
| AUTH-015 | 이미 탈퇴된 회원입니다                 | 409 Conflict          | 이미 탈퇴된 회원      |
| AUTH-016 | 입력값이 올바르지 않습니다               | 400 Bad Request       | 인증/보안 입력값 오류  |
| AUTH-017 | 비밀번호가 일치하지 않습니다              | 401 Unauthorized      | 비밀번호 불일치      |
| AUTH-018 | 인증되지 않은 이메일입니다.             | 401 Unauthorized      | 이메일 미인증        |

## User

| Code     | Message                | Status          | Description   |
|----------|------------------------|-----------------|---------------|
| USER-001 | 사용자를 찾을 수 없습니다 | 404 Not Found   | 사용자 없음    |
| USER-002 | 부상 레벨이 존재하지 않습니다 | 400 Bad Request | 부상 레벨 오류 |
| USER-003 | 알러지 타입이 존재하지 않습니다 | 400 Bad Request | 알레르기 타입 오류 |

## Community

| Code         | Message                        | Status          | Description     |
|--------------|--------------------------------|-----------------|-----------------|
| COMMUNITY-001 | 게시글을 찾을 수 없습니다          | 404 Not Found   | 게시글 없음      |
| COMMUNITY-002 | 게시글 수정 또는 삭제 권한이 없습니다 | 403 Forbidden   | 게시글 권한 없음   |
| COMMUNITY-003 | 댓글을 찾을 수 없습니다           | 404 Not Found   | 댓글 없음       |
| COMMUNITY-004 | 댓글 작성자만 수정 또는 삭제할 수 있습니다 | 403 Forbidden | 댓글 권한 없음   |
| COMMUNITY-005 | 잘못된 입력값입니다              | 400 Bad Request | 커뮤니티 입력값 오류 |
| COMMUNITY-006 | 본인의 게시글 또는 댓글은 신고할 수 없습니다 | 400 Bad Request | 자기 신고 금지   |
| COMMUNITY-007 | 공지사항은 신고할 수 없습니다        | 400 Bad Request | 공지 신고 금지   |
| COMMUNITY-008 | 이미 신고한 게시글 또는 댓글입니다     | 409 Conflict    | 중복 신고       |

## PT

| Code   | Message                                  | Status        | Description     |
|--------|------------------------------------------|---------------|-----------------|
| PT-001 | 예약이 불가능한 상태입니다                   | 409 Conflict  | 예약 불가 상태   |
| PT-002 | 방의 정원이 초과되었습니다                   | 409 Conflict  | 정원 초과       |
| PT-003 | 이미 예약된 상태입니다                     | 409 Conflict  | 중복 예약       |
| PT-004 | 참여 코드가 일치하지 않습니다               | 403 Forbidden | 참여 코드 불일치 |
| PT-005 | 진행 중이거나 종료된 방의 예약은 취소할 수 없습니다 | 409 Conflict | 취소 불가       |
| PT-006 | 해당 사용자는 현재 방에 참여 중이 아닙니다      | 409 Conflict  | 참여 중 아님    |
| PT-007 | 존재하지 않는 방입니다.                    | 404 Not Found | 방 없음        |
| KICKED | 강퇴된 방에는 다시 입장할 수 없습니다.         | 403 Forbidden | 강퇴된 방 재입장 불가 |

## Exercise

| Code        | Message              | Status        | Description |
|-------------|----------------------|---------------|-------------|
| EXERCISE-001 | 운동을 찾을 수 없습니다 | 404 Not Found | 운동 없음    |

## Food

| Code     | Message             | Status        | Description |
|----------|---------------------|---------------|-------------|
| FOOD-001 | 음식을 찾을 수 없습니다 | 404 Not Found | 음식 없음    |

## Workout

| Code        | Message                       | Status        | Description |
|-------------|-------------------------------|---------------|-------------|
| WORKOUT-001 | 해당 날짜의 운동 계획이 없습니다 | 404 Not Found | 운동 계획 없음 |
| WORKOUT-002 | 운동 항목을 찾을 수 없습니다     | 404 Not Found | 운동 항목 없음 |

## Diet

| Code     | Message                      | Status        | Description |
|----------|------------------------------|---------------|-------------|
| DIET-001 | 해당 날짜의 식단 기록이 없습니다 | 404 Not Found | 식단 기록 없음 |
| DIET-002 | 식단 항목을 찾을 수 없습니다     | 404 Not Found | 식단 항목 없음 |

## Trainer

| Code        | Message                 | Status        | Description |
|-------------|-------------------------|---------------|-------------|
| TRAINER-001 | 트레이너 권한이 필요합니다 | 403 Forbidden | 트레이너 권한 필요 |

## Admin

| Code      | Message                 | Status        | Description |
|-----------|-------------------------|---------------|-------------|
| ADMIN-001 | 이미 차단된 회원입니다     | 409 Conflict  | 이미 차단    |
| ADMIN-002 | 차단되지 않은 회원입니다   | 409 Conflict  | 차단 아님    |
| ADMIN-003 | 관리자는 차단할 수 없습니다 | 403 Forbidden | 관리자 차단 불가 |
| ADMIN-004 | 트레이너 신청 내역이 없습니다 | 404 Not Found | 신청 내역 없음 |
| ADMIN-005 | 거절 사유를 입력해주세요    | 400 Bad Request | 거절 사유 필요 |
| ADMIN-006 | 신고 내역을 찾을 수 없습니다 | 404 Not Found | 신고 없음    |

## Upload

| Code      | Message                    | Status                    | Description        |
|-----------|----------------------------|---------------------------|--------------------|
| UPLOAD-001 | 지원하지 않는 업로드 타입입니다 | 400 Bad Request           | 업로드 타입 오류     |
| UPLOAD-002 | 지원하지 않는 파일 형식입니다  | 400 Bad Request           | 파일 확장자 오류     |
| UPLOAD-003 | 파일 크기가 10MB를 초과합니다 | 400 Bad Request           | 파일 크기 초과      |
| UPLOAD-004 | 업로드 URL 생성에 실패했습니다 | 500 Internal Server Error | Presigned URL 실패 |
