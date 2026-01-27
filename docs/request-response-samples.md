# Request/Response Samples

모든 응답은 공통 `ApiResponse` 포맷을 따릅니다. 예시는 대표 케이스이며, 필드는 상황에 따라 일부 생략/확장될 수 있습니다.

## Auth - Signup

### Request
```http
POST /api/auth/signup
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd!",
  "nickname": "user",
  "phoneNumber": "010-1234-5678",
  "profileImageUrl": "https://cdn.example.com/profile.png"
}
```

### Response
```json
{
  "success": true,
  "data": {
    "accessToken": "<ACCESS_TOKEN>",
    "refreshToken": "<REFRESH_TOKEN>",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "error": null
}
```

## Auth - Login

### Request
```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd!"
}
```

### Response
```json
{
  "success": true,
  "data": {
    "accessToken": "<ACCESS_TOKEN>",
    "refreshToken": "<REFRESH_TOKEN>",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "error": null
}
```

## Auth - Token Reissue

### Request
```http
POST /api/auth/token/reissue
Content-Type: application/json
```

```json
{
  "refreshToken": "<REFRESH_TOKEN>"
}
```

### Response
```json
{
  "success": true,
  "data": {
    "accessToken": "<NEW_ACCESS_TOKEN>",
    "refreshToken": "<NEW_REFRESH_TOKEN>",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "error": null
}
```

## Me - Get Profile (Auth)

### Request
```http
GET /api/me
Authorization: Bearer <ACCESS_TOKEN>
```

### Response
```json
{
  "success": true,
  "data": {
    "email": "user@example.com",
    "handle": "user123",
    "nickname": "user",
    "role": "USER",
    "status": "ACTIVE",
    "profileImageUrl": "https://cdn.example.com/profile.png",
    "phoneNumber": "010-1234-5678",
    "createdAt": "2025-01-02T03:04:05Z",
    "updatedAt": "2025-01-10T08:00:00Z"
  },
  "error": null
}
```

## Upload - Presigned URL (Auth)

### Request
```http
POST /api/upload/presigned-url
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

```json
{
  "uploadType": "PROFILE_IMAGE",
  "fileName": "profile.png",
  "fileSize": 123456,
  "contentType": "image/png"
}
```

### Response
```json
{
  "success": true,
  "data": {
    "presignedUrl": "https://s3...",
    "fileUrl": "https://bucket.s3.region.amazonaws.com/...",
    "fileKey": "uploads/2025/01/profile.png"
  },
  "error": null
}
```

## PT - Create Room (Auth)

### Request
```http
POST /api/pt-rooms/create
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

```json
{
  "roomType": "ONE_ON_ONE",
  "title": "하체 집중 PT",
  "description": "무릎 부담 최소화",
  "scheduledAt": "2025-02-01T10:00:00Z",
  "maxParticipants": 3,
  "isPrivate": true
}
```

### Response
```json
{
  "success": true,
  "data": {
    "ptRoomId": 1001,
    "title": "하체 집중 PT",
    "description": "무릎 부담 최소화",
    "scheduledAt": "2025-02-01T10:00:00Z",
    "trainer": {
      "nickname": "trainer",
      "handle": "trainer01",
      "profileImageUrl": "https://cdn.example.com/trainer.png",
      "bio": "10년 경력"
    },
    "entryCode": "123456",
    "isPrivate": true,
    "roomType": "ONE_ON_ONE",
    "status": "SCHEDULED",
    "janusRoomKey": "room-abc",
    "maxParticipants": 3,
    "participants": {
      "count": 1,
      "users": [
        { "nickname": "trainer", "handle": "trainer01" }
      ]
    }
  },
  "error": null
}
```

## Diet - Foods List (Public)

### Request
```http
GET /api/foods?cursor=0&limit=10
```

### Response
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "foodId": 10,
        "name": "닭가슴살",
        "imageUrl": "https://cdn.example.com/food.png",
        "allergyCodes": "",
        "calories": 165,
        "carbs": 0.0,
        "protein": 31.0,
        "fat": 3.6
      }
    ],
    "nextCursor": 10,
    "hasNext": true
  },
  "error": null
}
```

## Diet - Day Detail (Auth)

### Request
```http
GET /api/me/diets/days/2025-02-01
Authorization: Bearer <ACCESS_TOKEN>
```

### Response
```json
{
  "success": true,
  "data": {
    "date": "2025-02-01",
    "dietDayId": 2001,
    "meals": [
      {
        "dietMealId": 3001,
        "sortOrder": 1,
        "items": [
          {
            "dietMealItemId": 4001,
            "foodId": 10,
            "name": "닭가슴살",
            "calories": 165,
            "carbs": 0.0,
            "proteins": 31.0,
            "fats": 3.6
          }
        ]
      }
    ]
  },
  "error": null
}
```

## Workout - Day Detail (Auth)

### Request
```http
GET /api/me/workouts/days/2025-02-01
Authorization: Bearer <ACCESS_TOKEN>
```

### Response
```json
{
  "success": true,
  "data": {
    "date": "2025-02-01",
    "workoutDayId": 5001,
    "title": "상체 루틴",
    "totalMinutes": 60,
    "exerciseCount": 4,
    "completedCount": 1,
    "items": [
      {
        "workoutItemId": 6001,
        "exerciseId": 101,
        "name": "벤치프레스",
        "quantity": 10,
        "sets": 3,
        "restSeconds": 90,
        "isChecked": false,
        "sortOrder": 1
      }
    ]
  },
  "error": null
}
```

## Community - Post Detail (Public)

### Request
```http
GET /api/board/posts/123
```

### Response
```json
{
  "success": true,
  "data": {
    "postId": 123,
    "author": { "nickname": "user", "handle": "user123" },
    "category": "FREE",
    "isNotice": false,
    "title": "운동 루틴 공유",
    "viewCount": 120,
    "commentCount": 2,
    "content": "내용입니다",
    "status": "POSTED",
    "createdAt": "2025-01-02T03:04:05Z",
    "updatedAt": "2025-01-02T03:10:00Z",
    "deletedAt": null,
    "comments": [
      {
        "commentId": 1,
        "content": "좋은 글이에요",
        "author": { "nickname": "user2", "handle": "user234" },
        "createdAt": "2025-01-02T04:00:00Z",
        "updatedAt": "2025-01-02T04:00:00Z",
        "deletedAt": null,
        "children": []
      }
    ]
  },
  "error": null
}
```
