package com.electricity.auth.service;

import com.electricity.auth.model.AuthResult;
import com.electricity.auth.model.UserRole;

/**
 * Service interface for handling user and administrator authentication.
 * Keeps the business and authentication contract cleanly separated from the Swing GUI.
 */
public interface AuthService {

    /**
     * Authenticates a user with the given credentials and requested role.
     *
     * @param username the username or account identifier
     * @param password the plain text password characters; callers should clear the array after use
     * @param role     the intended role (USER or ADMIN)
     * @return an AuthResult indicating success or failure with detail message
     */
    AuthResult authenticate(String username, char[] password, UserRole role);
}
