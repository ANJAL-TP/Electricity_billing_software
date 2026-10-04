package com.electricity.admindashboard;

import com.electricity.admindashboard.model.AdminProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class AdminDashboardTest {

    private AdminDashboard dashboard;

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AdminProfile profile = new AdminProfile(
                    "ADM-TEST-01",
                    "testadmin",
                    "Test Administrator",
                    "testadmin@ebs.com",
                    "Quality Assurance",
                    "Today, 10:00 AM"
            );
            dashboard = new AdminDashboard(profile);
        });
    }

    @Test
    @DisplayName("Should initialize AdminDashboard window and subpanels properly")
    void testWindowInitialization() {
        assertNotNull(dashboard);
        assertNotNull(dashboard.getDashboardPanel());
        assertNotNull(dashboard.getProfilePanel());
        assertTrue(dashboard.isResizable(), "Dashboard window should be resizable");
        assertEquals("Electricity Billing System — Admin Dashboard Console", dashboard.getTitle());
    }

    @Test
    @DisplayName("Should smoothly handle navigation switching without exception")
    void testNavigationSwitching() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertDoesNotThrow(() -> dashboard.onNavigate(SidebarPanel.VIEW_CONSUMER_MANAGEMENT));
            assertDoesNotThrow(() -> dashboard.onNavigate(SidebarPanel.VIEW_METER_READING));
            assertDoesNotThrow(() -> dashboard.onNavigate(SidebarPanel.VIEW_BILL_GENERATION));
            assertDoesNotThrow(() -> dashboard.onNavigate(SidebarPanel.VIEW_BILL_HISTORY));
            assertDoesNotThrow(() -> dashboard.onNavigate(SidebarPanel.VIEW_PAYMENTS));
            assertDoesNotThrow(() -> dashboard.onNavigate(SidebarPanel.VIEW_REPORTS));
            assertDoesNotThrow(() -> dashboard.onNavigate(SidebarPanel.VIEW_PROFILE));
            assertDoesNotThrow(() -> dashboard.onNavigate(SidebarPanel.VIEW_DASHBOARD));
        });
    }

    @Test
    @DisplayName("Profile panel should reflect current admin details")
    void testProfileDetails() {
        ProfilePanel profilePanel = dashboard.getProfilePanel();
        assertNotNull(profilePanel);
        assertEquals("testadmin", profilePanel.getProfile().username());
        assertEquals("Test Administrator", profilePanel.getProfile().fullName());
        assertEquals("testadmin@ebs.com", profilePanel.getProfile().email());
    }
}
