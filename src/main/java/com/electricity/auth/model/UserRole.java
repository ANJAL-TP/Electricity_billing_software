package com.electricity.auth.model;

/**
 * Enumeration representing user roles supported by the system.
 */
public enum UserRole {
    USER("User Login"),
    ADMIN("Admin Login");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
