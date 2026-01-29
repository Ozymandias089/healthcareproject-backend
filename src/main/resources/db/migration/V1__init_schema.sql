-- V1__init_schema.sql
-- (1) tables, PK/UK, entity-defined indexes
-- (2) FK는 V2에서 add constraint

-- =========================
-- users
-- =========================
create table users (
                       user_id bigserial primary key,
                       email varchar(255) not null unique,
                       handle varchar(20) not null unique,
                       password_hash varchar(255),
                       nickname varchar(50) not null,
                       phone_number varchar(20),
                       role varchar(20) not null,
                       status varchar(20) not null,
                       profile_image_url varchar(2048),
                       email_verified boolean not null default false,

                       created_at timestamptz not null,
                       updated_at timestamptz,
                       deleted_at timestamptz
);

-- =========================
-- social_accounts
-- =========================
create table social_accounts (
                                 social_account_id bigserial primary key,
                                 user_id bigint not null,
                                 provider varchar(20) not null,
                                 provider_user_id varchar(255) not null,
                                 connected_at timestamptz not null,

                                 constraint uk_social_provider_user unique (provider, provider_user_id),
                                 constraint uk_social_user_provider unique (user_id, provider)
);

create index idx_social_user on social_accounts(user_id);

-- =========================
-- user_profiles (PK=FK)
-- =========================
create table user_profiles (
                               user_id bigint primary key,
                               height_cm int,
                               weight_kg int,
                               age int,
                               gender varchar(20),
                               experience_level varchar(20),
                               goal_type varchar(20),
                               weekly_days int,
                               session_minutes int,

                               created_at timestamptz not null,
                               updated_at timestamptz,
                               deleted_at timestamptz
);

-- ElementCollection: user_profile_allergies
create table user_profile_allergies (
                                        user_id bigint not null,
                                        allergy varchar(30) not null,
                                        primary key (user_id, allergy)
);

-- =========================
-- user_injuries
-- =========================
create table user_injuries (
                               injury_id bigserial primary key,
                               user_id bigint not null,
                               injury_part varchar(100) not null,
                               injury_level varchar(20) not null,

                               created_at timestamptz not null,
                               updated_at timestamptz,
                               deleted_at timestamptz
);

create index idx_injuries_user on user_injuries(user_id);

-- =========================
-- trainer_info (PK=FK)
-- =========================
create table trainer_info (
                              user_id bigint primary key,
                              application_status varchar(20) not null,
                              license_urls_json text,
                              bio text,
                              reject_reason text,
                              approved_at timestamptz,

                              created_at timestamptz not null,
                              updated_at timestamptz,
                              deleted_at timestamptz
);

create index idx_trainer_status on trainer_info(application_status);

-- =========================
-- calendar_day_notes
-- =========================
create table calendar_day_notes (
                                    calendar_day_note_id bigserial primary key,
                                    user_id bigint not null,
                                    note_date date not null,
                                    note text not null,

                                    created_at timestamptz not null,
                                    updated_at timestamptz,
                                    deleted_at timestamptz,

                                    constraint uk_calendar_note_user_date unique (user_id, note_date)
);

create index idx_calendar_note_user_date on calendar_day_notes(user_id, note_date);

-- =========================
-- posts
-- =========================
create table posts (
                       post_id bigserial primary key,
                       user_id bigint not null,
                       category varchar(30) not null,
                       title varchar(200) not null,
                       content text not null,
                       status varchar(20) not null,
                       view_count bigint not null,
                       is_notice boolean not null,

                       created_at timestamptz not null,
                       updated_at timestamptz,
                       deleted_at timestamptz
);

-- =========================
-- comments
-- =========================
create table comments (
                          comment_id bigserial primary key,
                          post_id bigint not null,
                          user_id bigint not null,
                          parent_comment_id bigint,
                          content text not null,
                          status varchar(20) not null,

                          created_at timestamptz not null,
                          updated_at timestamptz,
                          deleted_at timestamptz
);

-- =========================
-- reports
-- =========================
create table reports (
                         report_id bigserial primary key,
                         reporter_id bigint not null,
                         type varchar(20) not null,
                         target_id bigint not null,
                         reason text not null,
                         status varchar(20) not null,

                         created_at timestamptz not null,
                         updated_at timestamptz,
                         deleted_at timestamptz,

                         constraint uk_report_reporter_target unique (reporter_id, type, target_id)
);

create index idx_report_status on reports(status);
create index idx_report_type on reports(type);

-- =========================
-- foods
-- =========================
create table foods (
                       food_id bigserial primary key,
                       name varchar(150) not null,
                       image_url varchar(2048),
                       nutrition_unit varchar(20) not null,
                       nutrition_amount int not null,
                       calories int not null,
                       carbs numeric(10,2),
                       protein numeric(10,2),
                       fat numeric(10,2),
                       display_serving varchar(100),
                       allergy_codes varchar(1000),
                       is_active boolean not null,

                       created_at timestamptz not null,
                       updated_at timestamptz,
                       deleted_at timestamptz
);

-- =========================
-- diet_days
-- =========================
create table diet_days (
                           diet_day_id bigserial primary key,
                           user_id bigint not null,
                           log_date date not null,

                           created_at timestamptz not null,
                           updated_at timestamptz,
                           deleted_at timestamptz,

                           constraint uk_diet_days_user_date unique (user_id, log_date)
);

-- =========================
-- diet_meals
-- =========================
create table diet_meals (
                            diet_meal_id bigserial primary key,
                            diet_meal_title text not null,
                            diet_day_id bigint not null,
                            sort_order int not null,

                            constraint uk_diet_meals_day_sort unique (diet_day_id, sort_order)
);

-- =========================
-- diet_meal_items
-- =========================
create table diet_meal_items (
                                 diet_meal_item_id bigserial primary key,
                                 diet_meal_id bigint not null,
                                 food_id bigint not null,
                                 count int not null,
                                 is_checked boolean not null
);

create index idx_diet_items_meal on diet_meal_items(diet_meal_id);
create index idx_diet_items_food on diet_meal_items(food_id);

-- =========================
-- exercises
-- =========================
create table exercises (
                           exercise_id bigserial primary key,
                           name varchar(100) not null,
                           body_part varchar(20),
                           difficulty varchar(20),
                           image_url varchar(2048),
                           description text not null,
                           precautions text,
                           youtube_url varchar(2048),
                           is_active boolean not null,

                           created_at timestamptz not null,
                           updated_at timestamptz,
                           deleted_at timestamptz
);

-- =========================
-- workout_days
-- =========================
create table workout_days (
                              workout_day_id bigserial primary key,
                              user_id bigint not null,
                              log_date date not null,
                              total_minutes int not null,
                              title text not null,

                              created_at timestamptz not null,
                              updated_at timestamptz,
                              deleted_at timestamptz,

                              constraint uk_workout_days_user_date unique (user_id, log_date)
);

-- =========================
-- workout_items
-- =========================
create table workout_items (
                               workout_item_id bigserial primary key,
                               workout_day_id bigint not null,
                               exercise_id bigint not null,
                               sort_order int not null,
                               sets int,
                               reps int,
                               duration_minutes int,
                               distance_km numeric(10,2),
                               rest_second int,
                               rpe int,
                               amount text,
                               is_checked boolean not null
);

create index idx_workout_items_day_sort on workout_items(workout_day_id, sort_order);
create index idx_workout_items_exercise on workout_items(exercise_id);

-- =========================
-- pt_rooms
-- =========================
create table pt_rooms (
                          pt_room_id bigserial primary key,
                          trainer_id bigint not null,
                          title varchar(200) not null,
                          description text,
                          room_type varchar(20) not null,
                          scheduled_start_at timestamptz,
                          started_at timestamptz,
                          max_participants int,
                          is_private boolean not null,
                          entry_code varchar(255),
                          status varchar(20) not null,

                          created_at timestamptz not null,
                          updated_at timestamptz,
                          deleted_at timestamptz
);

-- =========================
-- pt_room_participants
-- =========================
create table pt_room_participants (
                                      pt_room_participant_id bigserial primary key,
                                      pt_room_id bigint not null,
                                      user_id bigint not null,
                                      status varchar(20) not null,
                                      joined_at timestamptz not null,
                                      left_at timestamptz,

                                      created_at timestamptz not null,
                                      updated_at timestamptz,
                                      deleted_at timestamptz,

                                      constraint uk_pt_room_participant_room_user unique (pt_room_id, user_id)
);

create index idx_pt_room_participant_user on pt_room_participants(user_id);
create index idx_pt_room_participant_room_status on pt_room_participants(pt_room_id, status);

-- =========================
-- pt_janus_room_keys
-- =========================
create table pt_janus_room_keys (
                                    room_key int primary key,
                                    status varchar(20) not null,
                                    pt_room_id bigint,
                                    allocated_at timestamptz,
                                    released_at timestamptz,

                                    created_at timestamptz not null,
                                    updated_at timestamptz,
                                    deleted_at timestamptz,

                                    constraint uk_pt_janus_keys_pt_room unique (pt_room_id)
);

create index idx_pt_janus_keys_status on pt_janus_room_keys(status);
