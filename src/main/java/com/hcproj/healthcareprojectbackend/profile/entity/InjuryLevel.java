package com.hcproj.healthcareprojectbackend.profile.entity;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;

public enum InjuryLevel {
    MILD, CAUTION, SEVERE;

    public static InjuryLevel from(String value) {
        try {
            return InjuryLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INJURY_LEVEL);
        }
    }
}
