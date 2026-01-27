# API Endpoints

## 엔드포인트 목록(요약)

#### Health
| Method | Path         | Description | Auth   |
|--------|--------------|-------------|--------|
| GET    | /api/health  | 헬스 체크       | Public |
| GET    | /api/version | 버전 정보       | Public |

#### Auth
| Method | Path                             | Description    | Auth   |
|--------|----------------------------------|----------------|--------|
| POST   | /api/auth/signup                 | 회원가입           | Public |
| POST   | /api/auth/email/check            | 이메일 중복 체크      | Public |
| POST   | /api/auth/login                  | 로그인            | Public |
| POST   | /api/auth/token/reissue          | 토큰 재발급         | Public |
| POST   | /api/auth/logout                 | 로그아웃           | Public |
| POST   | /api/auth/social/login           | 소셜 로그인         | Public |
| POST   | /api/auth/password/reset/request | 비밀번호 재설정 메일 발송 | Public |
| POST   | /api/auth/password/reset         | 비밀번호 재설정       | Public |
| POST   | /api/auth/email/verify/request   | 이메일 인증 코드 발송   | Public |
| POST   | /api/auth/email/verify/confirm   | 이메일 인증 코드 확인   | Public |

#### Me
| Method | Path                                                 | Description    | Auth |
|--------|------------------------------------------------------|----------------|------|
| GET    | /api/me                                              | 내 기본정보 조회      | Auth |
| DELETE | /api/me                                              | 회원탈퇴           | Auth |
| GET    | /api/me/trainer                                      | 내 트레이너 정보 조회   | Auth |
| PATCH  | /api/me/password                                     | 비밀번호 변경        | Auth |
| PUT    | /api/me/onboarding                                   | 온보딩 저장         | Auth |
| PATCH  | /api/me/nickname                                     | 닉네임 변경         | Auth |
| PATCH  | /api/me/phone                                        | 전화번호 변경        | Auth |
| PATCH  | /api/me/profile-image                                | 프로필 이미지 변경     | Auth |
| GET    | /api/me/onboarding/status                            | 온보딩 상태 조회      | Auth |
| GET    | /api/me/profile                                      | 신체정보 조회        | Auth |
| GET    | /api/me/injuries                                     | 부상정보 조회        | Auth |
| GET    | /api/me/allergies                                    | 알레르기 조회        | Auth |
| GET    | /api/me/diets/days/{date}                            | 날짜별 식단 조회      | Auth |
| GET    | /api/me/workouts/days/{date}                         | 날짜별 운동 계획 조회   | Auth |
| PATCH  | /api/me/workouts/workout-items/{workoutItemId}/check | 운동 항목 체크 상태 변경 | Auth |
| POST   | /api/me/social/connect                               | 소셜 연동          | Auth |
| POST   | /api/me/social/disconnect                            | 소셜 연동 해제       | Auth |
| GET    | /api/me/social                                       | 연결된 소셜 조회      | Auth |

#### Trainer
| Method | Path                     | Description | Auth |
|--------|--------------------------|-------------|------|
| POST   | /api/trainer/application | 트레이너 신청     | Auth |
| PATCH  | /api/trainer/bio         | 트레이너 소개 수정  | Auth |

#### PT Rooms
| Method | Path                                  | Description | Auth |
|--------|---------------------------------------|-------------|------|
| POST   | /api/pt-rooms/create                  | PT 방 생성     | Auth |
| GET    | /api/pt-rooms                         | PT 방 목록 조회  | Auth |
| GET    | /api/pt-rooms/{ptRoomId}              | PT 방 상세 조회  | Auth |
| POST   | /api/pt-rooms/{ptRoomId}/join         | PT 방 참여     | Auth |
| POST   | /api/pt-rooms/{ptRoomId}/leave        | PT 방 나가기    | Auth |
| DELETE | /api/pt-rooms/{ptRoomId}              | PT 방 삭제     | Auth |
| PATCH  | /api/pt-rooms/{ptRoomId}/status       | PT 방 상태 변경  | Auth |
| GET    | /api/pt-rooms/{ptRoomId}/participants | PT 방 참가자 조회 | Auth |
| POST   | /api/pt-rooms/{ptRoomId}/kick         | PT 방 참가자 강퇴 | Auth |

#### Calendar
| Method | Path                     | Description | Auth |
|--------|--------------------------|-------------|------|
| PUT    | /api/memos/{date}        | 날짜 메모 저장    | Auth |
| GET    | /api/memos/{date}        | 날짜 메모 조회    | Auth |
| GET    | /api/me/calendar/weekly  | 주간 캘린더 요약   | Auth |
| GET    | /api/calendar/day/{date} | 일간 상세       | Auth |

#### Community
| Method | Path                                           | Description | Auth   |
|--------|------------------------------------------------|-------------|--------|
| GET    | /api/board/posts                               | 게시글 목록      | Public |
| GET    | /api/board/posts/{postId}                      | 게시글 상세      | Public |
| POST   | /api/board/posts                               | 게시글 작성      | Auth   |
| PATCH  | /api/board/posts/{postId}                      | 게시글 수정      | Auth   |
| DELETE | /api/board/posts/{postId}                      | 게시글 삭제      | Auth   |
| POST   | /api/board/posts/{postId}/comments             | 댓글 작성       | Auth   |
| PATCH  | /api/board/posts/{postId}/comments/{commentId} | 댓글 수정       | Auth   |
| DELETE | /api/board/posts/{postId}/comments/{commentId} | 댓글 삭제       | Auth   |
| POST   | /api/board/report                              | 신고 접수       | Auth   |

#### Diet
| Method | Path                                        | Description       | Auth   |
|--------|---------------------------------------------|-------------------|--------|
| GET    | /api/foods                                  | 음식 목록             | Public |
| GET    | /api/foods/{foodId}                         | 음식 상세             | Public |
| POST   | /api/foods                                  | 음식 등록             | Admin  |
| DELETE | /api/foods/{foodId}                         | 음식 삭제             | Admin  |
| PUT    | /api/diets/ai/week-plans                    | AI 식단 주간 플랜 생성/교체 | Auth   |
| PATCH  | /api/diet-meal-items/{dietMealItemId}/check | 식단 항목 체크 상태 변경    | Auth   |

#### Workout
| Method | Path                        | Description    | Auth   |
|--------|-----------------------------|----------------|--------|
| GET    | /api/exercises              | 운동 목록          | Public |
| GET    | /api/exercises/{exerciseId} | 운동 상세          | Public |
| POST   | /api/exercises              | 운동 등록          | Admin  |
| DELETE | /api/exercises/{exerciseId} | 운동 삭제          | Admin  |
| PUT    | /api/workouts/ai/routines   | AI 운동 루틴 생성/교체 | Auth   |

#### Upload
| Method | Path                      | Description      | Auth |
|--------|---------------------------|------------------|------|
| POST   | /api/upload/presigned-url | Presigned URL 발급 | Auth |

#### Admin
| Method | Path                                 | Description   | Auth  |
|--------|--------------------------------------|---------------|-------|
| GET    | /api/admin/dashboard                 | 관리자 대시보드      | Admin |
| GET    | /api/admin/users                     | 회원 목록         | Admin |
| PATCH  | /api/admin/users/promote             | 관리자 승격        | Admin |
| PATCH  | /api/admin/users/{userId}/status     | 회원 상태 변경      | Admin |
| PATCH  | /api/admin/trainer/{handle}/approve  | 트레이너 승인       | Admin |
| GET    | /api/admin/trainer/pending           | 트레이너 승인 대기 목록 | Admin |
| PATCH  | /api/admin/trainer/{handle}/reject   | 트레이너 거절       | Admin |
| GET    | /api/admin/reports                   | 신고 목록         | Admin |
| PATCH  | /api/admin/reports/{reportId}/status | 신고 상태 변경      | Admin |
| GET    | /api/admin/pt-rooms                  | PT 방 목록       | Admin |
| DELETE | /api/admin/pt-rooms/{ptRoomId}       | PT 방 강제 종료    | Admin |
| GET    | /api/admin/board                     | 게시판 통합 조회     | Admin |
| POST   | /api/admin/board/notice              | 공지 등록         | Admin |
