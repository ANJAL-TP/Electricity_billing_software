package com.electricity.auth.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.electricity.auth.model.AuthResult;
import com.electricity.auth.model.UserRole;
import com.electricity.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/** Authenticates active accounts against the users table and records successful logins. */
public final class JdbcAuthService implements AuthService {

    @Override
    public AuthResult authenticate(String username, char[] password, UserRole requestedRole) {
        if (username == null || username.isBlank()) {
            return AuthResult.failure("Username cannot be empty.");
        }
        if (password == null || password.length == 0) {
            return AuthResult.failure("Password cannot be empty.");
        }
        if (requestedRole == null) {
            return AuthResult.failure("Please select a login role (User or Admin).");
        }

        String normalizedUsername = username.trim();
        String passwordHash;
        String fullName;
        long userId;
        UserRole storedRole;
        String consumerStatus;

        try (Connection connection = DatabaseConnection.getConnection()) {
            String accountSql = """
                    SELECT u.id, u.password_hash, u.`role`, u.full_name,
                           c.status AS consumer_status
                    FROM users u
                    LEFT JOIN consumers c ON c.consumer_id = u.consumer_id
                    WHERE u.username = ? AND u.status = 'ACTIVE'
                    """;
            try (PreparedStatement statement = connection.prepareStatement(accountSql)) {
                statement.setString(1, normalizedUsername);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return AuthResult.failure("Invalid username or password.");
                    }
                    userId = result.getLong("id");
                    passwordHash = result.getString("password_hash");
                    storedRole = UserRole.valueOf(result.getString("role").toUpperCase(Locale.ROOT));
                    fullName = result.getString("full_name");
                    consumerStatus = result.getString("consumer_status");
                }
            }

            BCrypt.Result verification;
            try {
                verification = BCrypt.verifyer().verify(password, passwordHash);
            } catch (IllegalArgumentException exception) {
                return AuthResult.failure("Invalid username or password.");
            }
            if (!verification.validFormat || !verification.verified) {
                return AuthResult.failure("Invalid username or password.");
            }
            if (storedRole != requestedRole) {
                return AuthResult.failure("The selected role does not match this account.");
            }
            if (storedRole == UserRole.USER && !"ACTIVE".equalsIgnoreCase(consumerStatus)) {
                return AuthResult.failure("This account is not linked to an active consumer record.");
            }

            connection.setAutoCommit(false);
            try {
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?")) {
                    update.setLong(1, userId);
                    update.executeUpdate();
                }
                try (PreparedStatement activity = connection.prepareStatement(
                        "INSERT INTO activities (user_id, activity_type, description) VALUES (?, 'LOGIN', ?)")) {
                    activity.setLong(1, userId);
                    activity.setString(2, "Successful login: " + normalizedUsername);
                    activity.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

            String displayName = fullName == null || fullName.isBlank() ? normalizedUsername : fullName;
            return AuthResult.success(
                    normalizedUsername,
                    storedRole,
                    "Login successful! Welcome, " + displayName + " (" + storedRole.getDisplayName() + ")."
            );
        } catch (SQLException exception) {
            return AuthResult.failure("Unable to reach the database. Check that MySQL is running and configured.");
        }
    }
}
