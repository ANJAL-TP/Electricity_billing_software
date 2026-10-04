package com.electricity.auth.service;

import com.electricity.auth.model.AuthResult;
import com.electricity.auth.model.UserRole;

import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary mock implementation of {@link AuthService}.
 * Stores credentials in-memory for testing the GUI without requiring a MySQL connection.
 */
public class MockAuthService implements AuthService {

    private record UserAccount(String username, String password, UserRole role, String fullName) {}

    // In-memory credential repository
    private final Map<String, UserAccount> mockDatabase = new ConcurrentHashMap<>();

    public MockAuthService() {
        seedInitialAccounts();
    }

    private void seedInitialAccounts() {
        // Seed Administrator Accounts
        mockDatabase.put("admin", new UserAccount("admin", "admin123", UserRole.ADMIN, "System Administrator"));
        mockDatabase.put("manager", new UserAccount("manager", "admin@2024", UserRole.ADMIN, "Billing Manager"));

        // Seed Consumer / User Accounts
        mockDatabase.put("user", new UserAccount("user", "user123", UserRole.USER, "Standard Consumer"));
        mockDatabase.put("consumer101", new UserAccount("consumer101", "pass123", UserRole.USER, "Alice Johnson"));
        mockDatabase.put("john_doe", new UserAccount("john_doe", "electric123", UserRole.USER, "John Doe"));
    }

    @Override
    public AuthResult authenticate(String username, char[] password, UserRole role) {
        // Basic input validations
        if (username == null || username.trim().isEmpty()) {
            return AuthResult.failure("Username cannot be empty.");
        }

        if (password == null || password.length == 0) {
            return AuthResult.failure("Password cannot be empty.");
        }

        if (role == null) {
            return AuthResult.failure("Please select a login role (User or Admin).");
        }

        String normalizedUsername = username.trim().toLowerCase();
        UserAccount account = mockDatabase.get(normalizedUsername);

        if (account == null) {
            return AuthResult.failure("Invalid credentials. Account '" + username.trim() + "' does not exist.");
        }

        if (!Arrays.equals(account.password().toCharArray(), password)) {
            return AuthResult.failure("Invalid credentials. Incorrect password.");
        }

        if (account.role() != role) {
            return AuthResult.failure("Access Denied: Account is not authorized for " + role.getDisplayName() + ".");
        }

        return AuthResult.success(
                account.username(),
                account.role(),
                "Login successful! Welcome, " + account.fullName() + " (" + role.getDisplayName() + ")."
        );
    }

    /**
     * Helper method to register an additional mock account for testing.
     */
    public void registerAccount(String username, String password, UserRole role, String fullName) {
        if (username != null && !username.trim().isEmpty()) {
            mockDatabase.put(username.trim().toLowerCase(),
                    new UserAccount(username.trim(), password, role, fullName));
        }
    }
}
