# Healthcareproject-backend

Java Spring Boot 기반 헬스케어(운동/식단/커뮤니티/PT) 백엔드 프로젝트입니다.

- 개발 환경: H2(In-Memory)
- 운영 환경: Oracle DB(예정)
- 인증: JWT (principal = handle, details = userId)
- 공통 응답 포맷: ApiResponse
- 기능 단위 패키지 구조(auth / profile / trainer / calendar / pt / community ...)

---

## 스택

- Language: Java 21
- Framework: Spring Boot 3.5.x
- Security: Spring Security
- Data: Spring Data JPA, H2(개발), Oracle(운영 예정)
- Cache: Redis
- Auth: JWT (jjwt)
- Messaging: Spring Mail (SMTP)
- Cloud: AWS SDK (S3)
- AI: Spring AI OpenAI Starter
- Build: Gradle
- Test: JUnit 5, Spring Security Test

---

## 프로젝트 실행

### 1) 요구 사항
- Java 21
- Gradle

### 2) 실행

```bash
./gradlew bootRun
```

---

## 메뉴얼

### 환경 변수/설정
- 개발 환경은 H2를 사용합니다.
- `app.jwt.secret`는 최소 32자 이상을 권장합니다.
- 운영 환경 전환 시 Oracle 접속 정보 및 DDL 전략을 조정하세요.

### 테스트

```bash
./gradlew test
```

### 빌드

```bash
./gradlew build
```

### 로컬 H2 확인
- H2 Console URL: `/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`

---

## 패키지 구조

```
global/   : 공통 인프라(보안, 예외, 응답 포맷, 설정)
auth/     : 회원가입/로그인/토큰재발급/소셜로그인
profile/  : 프로필/부상
trainer/  : 트레이너 신청/승인
calendar/ : 날짜별 메모
pt/       : 화상PT 방/예약/참여자
community/: 게시판/댓글
diet/     : 음식/식단
workout/  : 운동/운동기록
```

---

## 공통 응답 포맷 (ApiResponse)

모든 API는 아래 포맷을 사용합니다.

* 성공:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

* 실패:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH-001",
    "message": "인증이 필요합니다."
  }
}
```

---

## 인증(JWT)

### 개요

* Authorization 헤더에 Bearer 토큰을 사용합니다.
* JWT의 `sub(subject)` = `handle`
* JWT claim `uid` = `userId`
* SecurityContext:

    * `principal` = handle(String)
    * `details` = userId(Number)

### Authorization 헤더 예시

```
Authorization: Bearer <accessToken>
```

---

## 컨트롤러에서 현재 사용자 주입하기 (권장)

Authentication/SecurityContext에 직접 접근하지 않고,
커스텀 어노테이션을 사용해 현재 사용자 정보를 주입받습니다.

* `@CurrentUserId` : 현재 인증된 사용자 userId(Long)
* `@CurrentHandle` : 현재 인증된 사용자 handle(String)

### 사용 예시

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public ApiResponse<String> me(
            @CurrentUserId Long userId,
            @CurrentHandle String handle
    ) {
        return ApiResponse.ok("userId=" + userId + ", handle=" + handle);
    }
}
```

### 주의사항

* 인증이 필요한 엔드포인트에서만 사용합니다.
* `permitAll` 엔드포인트에서 사용하면 UNAUTHORIZED 에러가 발생할 수 있습니다.

---

## API 문서

### 엔드포인트
- Base URL: `/api`
- 인증이 필요한 엔드포인트는 `Authorization: Bearer <accessToken>` 헤더가 필요합니다.
- `@AdminOnly`가 적용된 엔드포인트는 관리자 권한이 필요합니다.

### 상세 문서
- [엔드포인트 목록](docs/api-endpoints.md)
- [에러 코드 표](docs/error-codes.md)
- [환경변수 설정 예시](docs/env-vars.md)
- [요청/응답 샘플](docs/request-response-samples.md)

---

## Related Repositories

- **Backend**: https://github.com/Ozymandias089/healthcareproject-backend
- **Frontend**: https://github.com/juyoungck/healthcareproject-frontend

## Contributors

| Name    | GitHub                                             | Role         | Responsibility                                                                                                             |
|---------|----------------------------------------------------|--------------|----------------------------------------------------------------------------------------------------------------------------|
| **최영훈** | [@Ozymandias089](https://github.com/Ozymandias089) | Backend Lead | Architecture design, Infrastructure setup, Backend core (Auth, ME, PT, Calendar, Diet, Workout), AI integration, PR review |
| **안태호** | [@saesamn](https://github.com//saesamn)            | Backend      | Feature development(Board, PT, Admin)                                                                                      |
| **이현성** | [@HyunsEEE](https://github.com/HyunsEEE)           | Backend      | Feature development(Workout, Calendar, Admin, S3)                                                                          |
| **김주영** | [@juyoungck](https://github.com/juyoungck)         | Frontend PM  | UI, UX, API integration                                                                                                    |
| **박중건** | [@qkrwndrjs613](https://github.com/qkrwndrjs613)   | Frontend     | UI, UX, API Integration                                                                                                    |
| **백승진** | [@SeungjinB](https://github.com/SeungjinB)         | Frontend     | UI, UX, API Integration                                                                                                    |

