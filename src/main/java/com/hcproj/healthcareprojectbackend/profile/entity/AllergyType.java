package com.hcproj.healthcareprojectbackend.profile.entity;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;

public enum AllergyType {
    // 곡물
    WHEAT,          // 밀
    BUCKWHEAT,      // 메밀

    // 견과류
    PEANUT,         // 땅콩
    TREE_NUT,       // 견과류(호두, 아몬드 등)

    // 해산물
    CRUSTACEAN,     // 갑각류 (새우, 게)
    MOLLUSK,        // 연체류 (오징어, 조개)

    // 동물성
    EGG,            // 계란
    MILK,           // 우유
    BEEF,           // 소고기
    PORK,           // 돼지고기
    CHICKEN,        // 닭고기

    // 콩/씨앗
    SOY,            // 대두
    SESAME,         // 참깨

    // 기타
    FISH,           // 생선
    SULFITE;         // 아황산류

    public static AllergyType from(String value) {
        try {
            return AllergyType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_ALLERGY_TYPE);
        }
    }

}
