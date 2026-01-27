package com.hcproj.healthcareprojectbackend.auth.entity;

/** 사용자 권한(Role). 인가 정책은 Security 레이어에서 사용된다. */
public enum UserRole {
    USER, TRAINER, ADMIN
}
