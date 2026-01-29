-- V2__add_foreign_keys.sql

-- social_accounts.user_id -> users.user_id
alter table social_accounts
    add constraint fk_social_accounts_user
        foreign key (user_id) references users(user_id)
            on delete restrict;

-- user_profiles.user_id -> users.user_id (PK=FK)
alter table user_profiles
    add constraint fk_user_profiles_user
        foreign key (user_id) references users(user_id)
            on delete cascade;

-- user_profile_allergies.user_id -> user_profiles.user_id
alter table user_profile_allergies
    add constraint fk_user_profile_allergies_profile
        foreign key (user_id) references user_profiles(user_id)
            on delete cascade;

-- user_injuries.user_id -> users.user_id
alter table user_injuries
    add constraint fk_user_injuries_user
        foreign key (user_id) references users(user_id)
            on delete restrict;

-- trainer_info.user_id -> users.user_id (PK=FK)
alter table trainer_info
    add constraint fk_trainer_info_user
        foreign key (user_id) references users(user_id)
            on delete cascade;

-- calendar_day_notes.user_id -> users.user_id
alter table calendar_day_notes
    add constraint fk_calendar_day_notes_user
        foreign key (user_id) references users(user_id)
            on delete restrict;

-- posts.user_id -> users.user_id
alter table posts
    add constraint fk_posts_user
        foreign key (user_id) references users(user_id)
            on delete restrict;

-- comments.post_id -> posts.post_id
alter table comments
    add constraint fk_comments_post
        foreign key (post_id) references posts(post_id)
            on delete restrict;

-- comments.user_id -> users.user_id
alter table comments
    add constraint fk_comments_user
        foreign key (user_id) references users(user_id)
            on delete restrict;

-- comments.parent_comment_id -> comments.comment_id
alter table comments
    add constraint fk_comments_parent
        foreign key (parent_comment_id) references comments(comment_id)
            on delete set null;

-- reports.reporter_id -> users.user_id
alter table reports
    add constraint fk_reports_reporter
        foreign key (reporter_id) references users(user_id)
            on delete restrict;

-- diet_days.user_id -> users.user_id
alter table diet_days
    add constraint fk_diet_days_user
        foreign key (user_id) references users(user_id)
            on delete restrict;

-- diet_meals.diet_day_id -> diet_days.diet_day_id (강한 종속)
alter table diet_meals
    add constraint fk_diet_meals_day
        foreign key (diet_day_id) references diet_days(diet_day_id)
            on delete cascade;

-- diet_meal_items.diet_meal_id -> diet_meals.diet_meal_id (강한 종속)
alter table diet_meal_items
    add constraint fk_diet_meal_items_meal
        foreign key (diet_meal_id) references diet_meals(diet_meal_id)
            on delete cascade;

-- diet_meal_items.food_id -> foods.food_id
alter table diet_meal_items
    add constraint fk_diet_meal_items_food
        foreign key (food_id) references foods(food_id)
            on delete restrict;

-- workout_days.user_id -> users.user_id
alter table workout_days
    add constraint fk_workout_days_user
        foreign key (user_id) references users(user_id)
            on delete restrict;

-- workout_items.workout_day_id -> workout_days.workout_day_id (강한 종속)
alter table workout_items
    add constraint fk_workout_items_day
        foreign key (workout_day_id) references workout_days(workout_day_id)
            on delete cascade;

-- workout_items.exercise_id -> exercises.exercise_id
alter table workout_items
    add constraint fk_workout_items_exercise
        foreign key (exercise_id) references exercises(exercise_id)
            on delete restrict;

-- pt_rooms.trainer_id -> users.user_id
alter table pt_rooms
    add constraint fk_pt_rooms_trainer
        foreign key (trainer_id) references users(user_id)
            on delete restrict;

-- pt_room_participants.pt_room_id -> pt_rooms.pt_room_id
alter table pt_room_participants
    add constraint fk_pt_room_participants_room
        foreign key (pt_room_id) references pt_rooms(pt_room_id)
            on delete restrict;

-- pt_room_participants.user_id -> users.user_id
alter table pt_room_participants
    add constraint fk_pt_room_participants_user
        foreign key (user_id) references users(user_id)
            on delete restrict;

-- pt_janus_room_keys.pt_room_id -> pt_rooms.pt_room_id
alter table pt_janus_room_keys
    add constraint fk_pt_janus_room_keys_room
        foreign key (pt_room_id) references pt_rooms(pt_room_id)
            on delete set null;
