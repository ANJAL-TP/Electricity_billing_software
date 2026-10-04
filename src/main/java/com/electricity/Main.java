package com.electricity;

import com.electricity.auth.ui.LoginFrame;
import com.electricity.auth.ui.UIConstants;

import javax.swing.*;

/**
 * Main application entry point for the Electricity Billing System.
 */
public class Main {

    public static void main(String[] args) {
        // Configure anti-aliased font rendering for high-DPI modern displays
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Use System Look and Feel to match the host desktop operating system
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback gracefully to default Swing look and feel
        }

        UIConstants.installDefaults();

        // Launch the Login Frame on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
