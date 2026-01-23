package com.hcproj.healthcareprojectbackend.global.util;

public final class UtilityProvider {
    public static String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    public static String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }
}
