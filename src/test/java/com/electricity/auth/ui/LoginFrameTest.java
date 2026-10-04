package com.electricity.auth.ui;

import com.electricity.auth.service.MockAuthService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class LoginFrameTest {

    private LoginFrame loginFrame;

    @BeforeAll
    static void initHeadlessCheck() {
        // Ensure AWT runs properly
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Running in headless environment; skipping full GUI rendering assertions.");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            loginFrame = new LoginFrame(new MockAuthService());
        });
    }

    @Test
    @DisplayName("Should initialize UI components properly")
    void testComponentInitialization() {
        assertNotNull(loginFrame.getTxtUsername());
        assertNotNull(loginFrame.getTxtPassword());
        assertNotNull(loginFrame.getRbUserRole());
        assertNotNull(loginFrame.getRbAdminRole());
        assertNotNull(loginFrame.getLblStatusMessage());

        assertTrue(loginFrame.getRbUserRole().isSelected(), "User role should be selected by default");
        assertFalse(loginFrame.getRbAdminRole().isSelected());
    }

    @Test
    @DisplayName("Clear button should reset all input fields and status messages")
    void testClearFields() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            loginFrame.getTxtUsername().setText("testuser");
            loginFrame.getTxtPassword().setText("secretpass");
            loginFrame.getRbAdminRole().setSelected(true);
            loginFrame.getLblStatusMessage().setText("Error message");

            // Trigger clear
            loginFrame.clearFields();

            assertEquals("", loginFrame.getTxtUsername().getText());
            assertEquals(0, loginFrame.getTxtPassword().getPassword().length);
            assertTrue(loginFrame.getRbUserRole().isSelected());
            assertEquals(" ", loginFrame.getLblStatusMessage().getText());
        });
    }

    @Test
    @DisplayName("Validation fails when username is left empty")
    void testEmptyUsernameValidation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            loginFrame.getTxtUsername().setText("");
            loginFrame.getTxtPassword().setText("somepass");
            loginFrame.performLogin();

            assertTrue(loginFrame.getLblStatusMessage().getText().contains("enter your username"));
        });
    }

    @Test
    @DisplayName("Validation fails when password is left empty")
    void testEmptyPasswordValidation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            loginFrame.getTxtUsername().setText("admin");
            loginFrame.getTxtPassword().setText("");
            loginFrame.performLogin();

            assertTrue(loginFrame.getLblStatusMessage().getText().contains("enter your password"));
        });
    }
}
