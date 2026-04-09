package com.example.ddht.data.model;

public enum UserRole {
    MANAGER,
    STAFF,
    GUEST;

    public static UserRole fromString(String role) {
        if (role == null) {
            return GUEST;
        }
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return GUEST;
        }
    }
}
