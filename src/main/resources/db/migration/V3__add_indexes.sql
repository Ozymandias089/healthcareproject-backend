-- V3__add_indexes.sql

-- posts 목록조회: status='POSTED' + category + cursor(post_id desc)
create index if not exists idx_posts_status_postid_desc
    on posts(status, post_id desc);

create index if not exists idx_posts_category_status_postid_desc
    on posts(category, status, post_id desc);

-- comments: post 상세에서 post_id로 목록 조회가 잦음
create index if not exists idx_comments_post_id
    on comments(post_id);

-- diet/workout: user_id + log_date between 조회 자주
create index if not exists idx_diet_days_user_date
    on diet_days(user_id, log_date);

create index if not exists idx_workout_days_user_date
    on workout_days(user_id, log_date);

-- pt: 상태별/커서 조회 대비
create index if not exists idx_pt_rooms_status_pt_room_id_desc
    on pt_rooms(status, pt_room_id desc);

create index if not exists idx_pt_rooms_trainer_id
    on pt_rooms(trainer_id);
