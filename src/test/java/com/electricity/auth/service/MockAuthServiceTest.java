package com.electricity.auth.service;

import com.electricity.auth.model.AuthResult;
import com.electricity.auth.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockAuthServiceTest {

    private MockAuthService authService;

    @BeforeEach
    void setUp() {
        authService = new MockAuthService();
    }

    @Test
    @DisplayName("Should successfully authenticate valid User")
    void testValidUserAuthentication() {
        AuthResult result = authService.authenticate("user", "user123".toCharArray(), UserRole.USER);
        assertTrue(result.success());
        assertEquals("user", result.username());
        assertEquals(UserRole.USER, result.role());
        assertTrue(result.message().contains("Welcome"));
    }

    @Test
    @DisplayName("Should successfully authenticate valid Admin")
    void testValidAdminAuthentication() {
        AuthResult result = authService.authenticate("admin", "admin123".toCharArray(), UserRole.ADMIN);
        assertTrue(result.success());
        assertEquals("admin", result.username());
        assertEquals(UserRole.ADMIN, result.role());
        assertTrue(result.message().contains("Welcome"));
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void testIncorrectPassword() {
        AuthResult result = authService.authenticate("admin", "wrongpass".toCharArray(), UserRole.ADMIN);
        assertFalse(result.success());
        assertTrue(result.message().contains("Incorrect password"));
    }

    @Test
    @DisplayName("Should reject non-existent user account")
    void testNonExistentUser() {
        AuthResult result = authService.authenticate("nonexistent", "secret".toCharArray(), UserRole.USER);
        assertFalse(result.success());
        assertTrue(result.message().contains("does not exist"));
    }

    @Test
    @DisplayName("Should reject empty or blank username")
    void testBlankUsername() {
        AuthResult result = authService.authenticate("   ", "password".toCharArray(), UserRole.USER);
        assertFalse(result.success());
        assertTrue(result.message().contains("Username cannot be empty"));
    }

    @Test
    @DisplayName("Should reject empty password")
    void testEmptyPassword() {
        AuthResult result = authService.authenticate("admin", new char[0], UserRole.ADMIN);
        assertFalse(result.success());
        assertTrue(result.message().contains("Password cannot be empty"));
    }

    @Test
    @DisplayName("Should reject null role")
    void testNullRole() {
        AuthResult result = authService.authenticate("admin", "admin123".toCharArray(), null);
        assertFalse(result.success());
        assertTrue(result.message().contains("select a login role"));
    }

    @Test
    @DisplayName("Should reject User credentials attempting Admin login")
    void testRoleMismatchUserAsAdmin() {
        AuthResult result = authService.authenticate("user", "user123".toCharArray(), UserRole.ADMIN);
        assertFalse(result.success());
        assertTrue(result.message().contains("Access Denied"));
    }

    @Test
    @DisplayName("Should reject Admin credentials attempting User login")
    void testRoleMismatchAdminAsUser() {
        AuthResult result = authService.authenticate("admin", "admin123".toCharArray(), UserRole.USER);
        assertFalse(result.success());
        assertTrue(result.message().contains("Access Denied"));
    }

    @Test
    @DisplayName("Should support case-insensitive username lookup")
    void testCaseInsensitiveUsername() {
        AuthResult result = authService.authenticate("ADMIN", "admin123".toCharArray(), UserRole.ADMIN);
        assertTrue(result.success());
    }
}
