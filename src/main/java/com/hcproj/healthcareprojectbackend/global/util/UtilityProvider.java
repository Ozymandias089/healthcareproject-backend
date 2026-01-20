package com.hcproj.healthcareprojectbackend.global.util;

public final class UtilityProvider {
    public static String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
