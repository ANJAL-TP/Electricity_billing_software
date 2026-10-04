package com.electricity.auth.model;

/**
 * Immutable record representing the result of an authentication attempt.
 *
 * @param success  whether authentication succeeded
 * @param message  informational or error message
 * @param username authenticated username (null if failed)
 * @param role     authenticated role (null if failed)
 */
public record AuthResult(
        boolean success,
        String message,
        String username,
        UserRole role
) {
    public static AuthResult success(String username, UserRole role, String message) {
        return new AuthResult(true, message, username, role);
    }

    public static AuthResult failure(String message) {
        return new AuthResult(false, message, null, null);
    }
}
