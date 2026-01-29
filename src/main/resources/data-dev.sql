-- =========================
-- DEV RESET (H2)
-- =========================
TRUNCATE TABLE posts;
TRUNCATE TABLE exercises;
TRUNCATE TABLE foods;

ALTER TABLE exercises ALTER COLUMN exercise_id RESTART WITH 1;
ALTER TABLE foods ALTER COLUMN food_id RESTART WITH 1;
ALTER TABLE posts ALTER COLUMN post_id RESTART WITH 1;

-- =========================
-- USERS (dev seed) - idempotent
-- =========================
MERGE INTO users
    (email, handle, password_hash, nickname, phone_number, role, status, profile_image_url, email_verified, created_at, updated_at, deleted_at)
    KEY(email)
    VALUES
    ('user1@test.com', 'user1', '$2a$10$dev...', '유저1', '010-0000-0001', 'USER', 'ACTIVE', NULL, TRUE, NOW(), NOW(), NULL),
    ('user2@test.com', 'user2', '$2a$10$dev...', '유저2', '010-0000-0002', 'USER', 'ACTIVE', NULL, TRUE, NOW(), NOW(), NULL),
    ('user3@test.com', 'user3', '$2a$10$dev...', '유저3', '010-0000-0003', 'USER', 'ACTIVE', NULL, TRUE, NOW(), NOW(), NULL);

-- =========================
-- EXERCISES
-- =========================
INSERT INTO EXERCISES
(NAME, IMAGE_URL, DESCRIPTION, BODY_PART, DIFFICULTY, PRECAUTIONS, YOUTUBE_URL, IS_ACTIVE, CREATED_AT)
VALUES
    ('푸쉬업', 'https://images.unsplash.com/photo-1598971639058-a475f8e1c0ab?w=400', '가슴, 어깨, 삼두를 강화하는 대표적인 맨몸 운동입니다.', 'CHEST', 'BEGINNER', '손목이 꺾이지 않도록 주의하세요.', 'https://www.youtube.com/watch?v=example1', TRUE, NOW()),
    ('벤치프레스', 'https://images.unsplash.com/photo-1534368786749-b63e05c92717?w=400', '가슴 근육을 집중적으로 발달시키는 운동입니다.', 'CHEST', 'INTERMEDIATE', '바벨이 가슴에 닿을 때 반동을 주지 마세요.', 'https://www.youtube.com/watch?v=example2', TRUE, NOW()),
    ('덤벨 플라이', 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=400', '가슴 안쪽 라인을 만드는 데 효과적입니다.', 'CHEST', 'INTERMEDIATE', '팔꿈치를 살짝 굽힌 상태를 유지하세요.', 'https://www.youtube.com/watch?v=example3', TRUE, NOW()),
    ('풀업', 'https://images.unsplash.com/photo-1598971457999-ca4ef48a9a71?w=400', '등 근육 전체를 발달시키는 최고의 운동입니다.', 'BACK', 'ADVANCED', '반동을 사용하지 말고 천천히 수행하세요.', 'https://www.youtube.com/watch?v=example4', TRUE, NOW()),
    ('랫풀다운', 'https://images.unsplash.com/photo-1534368270820-9de3d8053204?w=400', '광배근을 집중적으로 자극하는 운동입니다.', 'BACK', 'BEGINNER', '어깨가 올라가지 않도록 주의하세요.', 'https://www.youtube.com/watch?v=example5', TRUE, NOW()),
    ('바벨 로우', 'https://images.unsplash.com/photo-1532029837206-abbe2b7620e3?w=400', '등 두께를 만드는 데 효과적인 운동입니다.', 'BACK', 'INTERMEDIATE', '허리를 곧게 유지하세요.', 'https://www.youtube.com/watch?v=example6', TRUE, NOW()),
    ('오버헤드 프레스', 'https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?w=400', '어깨 전면과 측면을 발달시키는 운동입니다.', 'SHOULDER', 'INTERMEDIATE', '허리가 과신전되지 않도록 복부에 힘을 주세요.', 'https://www.youtube.com/watch?v=example7', TRUE, NOW()),
    ('사이드 레터럴 레이즈', 'https://images.unsplash.com/photo-1581009146145-b5ef050c149a?w=400', '어깨 측면을 집중적으로 발달시킵니다.', 'SHOULDER', 'BEGINNER', '팔꿈치를 살짝 굽히고 어깨 높이까지만 올리세요.', 'https://www.youtube.com/watch?v=example8', TRUE, NOW()),
    ('바이셉 컬', 'https://images.unsplash.com/photo-1581009137042-c552e485697a?w=400', '이두근을 집중적으로 발달시키는 운동입니다.', 'ARM', 'BEGINNER', '팔꿈치를 고정하고 반동을 주지 마세요.', 'https://www.youtube.com/watch?v=example9', TRUE, NOW()),
    ('트라이셉 딥스', 'https://images.unsplash.com/photo-1598971639058-a475f8e1c0ab?w=400', '삼두근을 효과적으로 발달시키는 운동입니다.', 'ARM', 'INTERMEDIATE', '어깨가 앞으로 말리지 않도록 주의하세요.', 'https://www.youtube.com/watch?v=example10', TRUE, NOW()),
    ('스쿼트', 'https://images.unsplash.com/photo-1574680096145-d05b474e2155?w=400', '하체 전체를 발달시키는 대표적인 운동입니다.', 'LEG', 'BEGINNER', '무릎이 발끝을 넘어가지 않도록 주의하세요.', 'https://www.youtube.com/watch?v=example11', TRUE, NOW()),
    ('레그프레스', 'https://images.unsplash.com/photo-1434682881908-b43d0467b798?w=400', '대퇴사두근을 집중적으로 발달시킵니다.', 'LEG', 'BEGINNER', '허리가 등받이에서 떨어지지 않도록 하세요.', 'https://www.youtube.com/watch?v=example12', TRUE, NOW()),
    ('런지', 'https://images.unsplash.com/photo-1597452485669-2c7bb5fef90d?w=400', '하체 균형과 근력을 동시에 발달시킵니다.', 'LEG', 'INTERMEDIATE', '앞 무릎이 발끝을 넘지 않도록 하세요.', 'https://www.youtube.com/watch?v=example13', TRUE, NOW()),
    ('플랭크', 'https://images.unsplash.com/photo-1566241142559-40e1dab266c6?w=400', '코어 전체를 강화하는 기본 운동입니다.', 'CORE', 'BEGINNER', '엉덩이가 너무 올라가거나 내려가지 않도록 하세요.', 'https://www.youtube.com/watch?v=example14', TRUE, NOW()),
    ('크런치', 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400', '복직근 상부를 집중적으로 자극합니다.', 'CORE', 'BEGINNER', '목에 힘을 주지 말고 복부 힘으로 올라오세요.', 'https://www.youtube.com/watch?v=example15', TRUE, NOW()),
    ('버피', 'https://images.unsplash.com/photo-1599058917765-a780eda07a3e?w=400', '전신 유산소와 근력을 동시에 발달시킵니다.', 'FULL_BODY', 'ADVANCED', '착지할 때 무릎에 충격이 가지 않도록 주의하세요.', 'https://www.youtube.com/watch?v=example16', TRUE, NOW()),
    ('마운틴 클라이머', 'https://images.unsplash.com/photo-1598971639058-a475f8e1c0ab?w=400', '코어와 유산소를 동시에 훈련합니다.', 'FULL_BODY', 'INTERMEDIATE', '엉덩이가 너무 올라가지 않도록 하세요.', 'https://www.youtube.com/watch?v=example17', TRUE, NOW()),
    ('데드리프트', 'https://images.unsplash.com/photo-1534368420009-621bfab424a8?w=400', '후면 사슬 전체를 발달시키는 운동입니다.', 'BACK', 'ADVANCED', '허리를 둥글게 말지 않도록 주의하세요.', 'https://www.youtube.com/watch?v=example18', TRUE, NOW());

-- =========================
-- FOODS (Postgres, boolean)
-- =========================
INSERT INTO FOODS
(FOOD_ID, NAME, CALORIES, CARBS, PROTEIN, FAT, NUTRITION_AMOUNT, NUTRITION_UNIT, DISPLAY_SERVING,
 IS_ACTIVE, ALLERGY_CODES, IMAGE_URL, CREATED_AT, UPDATED_AT, DELETED_AT)
VALUES
    (1, '현미밥', 165, 34.5, 3.5, 1.2, 150, 'g', '1공기', TRUE, NULL, 'https://images.unsplash.com/photo-1516684732162-798a0062be99?w=300', NOW(), NOW(), NULL),
    (2, '닭가슴살 구이', 165, 0, 31, 3.6, 100, 'g', '1조각', TRUE, NULL, 'https://images.unsplash.com/photo-1632778149955-e80f8ceca2e8?w=300', NOW(), NOW(), NULL),
    (3, '계란 후라이', 90, 0.6, 6.3, 7, 50, 'g', '1개', TRUE, 'EGG', 'https://images.unsplash.com/photo-1525351484163-7529414344d8?w=300', NOW(), NOW(), NULL),
    (4, '삶은 계란', 77, 0.6, 6.3, 5.3, 50, 'g', '1개', TRUE, 'EGG', 'https://images.unsplash.com/photo-1482049016gy498f-713b7a0e94be?w=300', NOW(), NOW(), NULL),
    (5, '그릭요거트', 97, 3.6, 17.3, 0.7, 150, 'g', '1컵', TRUE, 'MILK', 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=300', NOW(), NOW(), NULL),
    (6, '바나나', 93, 23.5, 1.1, 0.3, 100, 'g', '1개', TRUE, NULL, 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=300', NOW(), NOW(), NULL),
    (7, '사과', 57, 14.1, 0.2, 0.4, 100, 'g', '1개', TRUE, NULL, 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=300', NOW(), NOW(), NULL),
    (8, '고구마', 128, 30, 1.4, 0.1, 100, 'g', '1개', TRUE, NULL, 'https://images.unsplash.com/photo-1596097635121-14b63a7a6c14?w=300', NOW(), NOW(), NULL),
    (9, '오트밀', 150, 27, 5, 2.5, 40, 'g', '1회분', TRUE, 'WHEAT', 'https://images.unsplash.com/photo-1517673400267-0251440c45dc?w=300', NOW(), NOW(), NULL),
    (10, '연어 구이', 208, 0, 20, 13, 100, 'g', '1토막', TRUE, 'FISH', 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=300', NOW(), NOW(), NULL),
    (11, '두부', 76, 1.9, 8, 4.2, 100, 'g', '반모', TRUE, 'SOY', 'https://images.unsplash.com/photo-1628689469838-524a4a973b8e?w=300', NOW(), NOW(), NULL),
    (12, '닭볶음탕', 180, 8, 18, 8, 200, 'g', '1인분', TRUE, NULL, 'https://images.unsplash.com/photo-1635451595512-a6e5bd5e5dd2?w=300', NOW(), NOW(), NULL),
    (13, '소고기 스테이크', 271, 0, 26, 18, 150, 'g', '1인분', TRUE, NULL, 'https://images.unsplash.com/photo-1600891964092-4316c288032e?w=300', NOW(), NOW(), NULL),
    (14, '삼겹살 구이', 518, 0, 15, 50, 150, 'g', '1인분', TRUE, NULL, 'https://images.unsplash.com/photo-1611489142329-5f62cfa43e6e?w=300', NOW(), NOW(), NULL),
    (15, '김치찌개', 120, 6, 8, 7, 300, 'g', '1인분', TRUE, 'SOY,FISH', 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=300', NOW(), NOW(), NULL),
    (16, '된장찌개', 100, 8, 6, 5, 300, 'g', '1인분', TRUE, 'SOY,FISH', 'https://images.unsplash.com/photo-1547592180-85f173990554?w=300', NOW(), NOW(), NULL),
    (17, '샐러드', 35, 7, 2, 0.3, 150, 'g', '1접시', TRUE, NULL, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=300', NOW(), NOW(), NULL),
    (18, '아보카도', 160, 8.5, 2, 14.7, 100, 'g', '반개', TRUE, NULL, 'https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=300', NOW(), NOW(), NULL),
    (19, '프로틴 쉐이크', 120, 3, 24, 1.5, 30, 'g', '1스쿱', TRUE, 'MILK,SOY', 'https://images.unsplash.com/photo-1622485831930-6a3c1d742b33?w=300', NOW(), NOW(), NULL),
    (20, '아몬드', 164, 6, 6, 14, 28, 'g', '한줌', TRUE, 'NUT', 'https://images.unsplash.com/photo-1508061253366-f7da158b6d46?w=300', NOW(), NOW(), NULL),
    (21, '브로콜리', 34, 6.6, 2.8, 0.4, 100, 'g', '1컵', TRUE, NULL, 'https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=300', NOW(), NOW(), NULL),
    (22, '시금치 나물', 45, 4, 3, 2, 100, 'g', '1접시', TRUE, NULL, 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=300', NOW(), NOW(), NULL),
    (23, '참치 통조림', 130, 0, 28, 1, 100, 'g', '1캔', TRUE, 'FISH', 'https://images.unsplash.com/photo-1558642452-9d2a7deb7f62?w=300', NOW(), NOW(), NULL),
    (24, '우유', 65, 4.8, 3.2, 3.6, 200, 'ml', '1컵', TRUE, 'MILK', 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=300', NOW(), NOW(), NULL),
    (25, '치즈', 113, 0.4, 7, 9.3, 30, 'g', '1장', TRUE, 'MILK', 'https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d?w=300', NOW(), NOW(), NULL),
    (26, '잡곡밥', 175, 36, 4, 1.5, 150, 'g', '1공기', TRUE, NULL, 'https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=300', NOW(), NOW(), NULL),
    (27, '콩나물국', 35, 3, 3, 1, 250, 'g', '1그릇', TRUE, 'SOY', 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=300', NOW(), NOW(), NULL),
    (28, '미역국', 45, 4, 2, 2, 250, 'g', '1그릇', TRUE, 'FISH', 'https://images.unsplash.com/photo-1569058242567-93de6f36f8eb?w=300', NOW(), NOW(), NULL),
    (29, '새우 볶음', 140, 3, 20, 5, 100, 'g', '1접시', TRUE, 'SHELLFISH', 'https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=300', NOW(), NOW(), NULL),
    (30, '오렌지', 47, 11.8, 0.9, 0.1, 100, 'g', '1개', TRUE, NULL, 'https://images.unsplash.com/photo-1547514701-42782101795e?w=300', NOW(), NOW(), NULL);

-- =========================
-- POSTS (no hard-coded user_id)
-- =========================
INSERT INTO posts (title, content, category, user_id, is_notice, view_count, status, created_at)
SELECT '테스트 게시글 1', '테스트 내용입니다 1', 'FREE', u.user_id, FALSE, 11, 'POSTED', NOW()
FROM users u WHERE u.handle = 'user2';

INSERT INTO posts (title, content, category, user_id, is_notice, view_count, status, created_at)
SELECT '테스트 게시글 2', '테스트 내용입니다 2', 'FREE', u.user_id, FALSE, 12, 'POSTED', NOW()
FROM users u WHERE u.handle = 'user3';

INSERT INTO posts (title, content, category, user_id, is_notice, view_count, status, created_at)
SELECT '테스트 게시글 3', '테스트 내용입니다 3', 'QUESTION', u.user_id, FALSE, 14, 'POSTED', NOW()
FROM users u WHERE u.handle = 'admin';

INSERT INTO posts (title, content, category, user_id, is_notice, view_count, status, created_at)
SELECT '공지사항 테스트', '공지사항 내용입니다', 'INFO', u.user_id, TRUE, 0, 'POSTED', NOW()
FROM users u WHERE u.handle = 'admin';
