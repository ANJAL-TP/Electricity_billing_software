package com.electricity.admindashboard.service;

import com.electricity.admindashboard.model.AdminProfile;
import com.electricity.admindashboard.model.DashboardStats;
import com.electricity.admindashboard.model.RecentActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MockAdminDashboardServiceTest {

    private MockAdminDashboardService service;

    @BeforeEach
    void setUp() {
        service = new MockAdminDashboardService();
    }

    @Test
    @DisplayName("Should return valid initial dashboard statistics")
    void testGetDashboardStats() {
        DashboardStats stats = service.getDashboardStats();
        assertNotNull(stats);
        assertEquals(12450, stats.totalConsumers());
        assertEquals(8920, stats.totalBills());
        assertEquals(7640, stats.paidBills());
        assertEquals(1280, stats.unpaidBills());
        assertEquals(458920.00, stats.totalRevenue(), 0.001);

        assertTrue(stats.formattedConsumers().contains("12,450"));
        assertTrue(stats.formattedRevenue().contains("458,920"));
        assertTrue(stats.paidPercentage() > 85.0);
    }

    @Test
    @DisplayName("Should return populated list of recent activities")
    void testGetRecentActivities() {
        List<RecentActivity> activities = service.getRecentActivities();
        assertNotNull(activities);
        assertFalse(activities.isEmpty());
        assertTrue(activities.size() >= 4);

        boolean foundConsumer = activities.stream().anyMatch(a -> a.title().contains("consumer"));
        boolean foundBill = activities.stream().anyMatch(a -> a.title().contains("Bill"));
        boolean foundPayment = activities.stream().anyMatch(a -> a.title().contains("Payment"));
        boolean foundMeter = activities.stream().anyMatch(a -> a.title().contains("Meter"));

        assertTrue(foundConsumer, "Should contain consumer activity");
        assertTrue(foundBill, "Should contain billing activity");
        assertTrue(foundPayment, "Should contain payment activity");
        assertTrue(foundMeter, "Should contain meter reading activity");
    }

    @Test
    @DisplayName("Should fetch existing admin profile and allow updates")
    void testAdminProfileOperations() {
        AdminProfile profile = service.getAdminProfile("admin");
        assertNotNull(profile);
        assertEquals("ADM-2024-001", profile.adminId());
        assertEquals("admin", profile.username());
        assertEquals("admin@ebs.com", profile.email());

        // Test update
        AdminProfile updated = profile.withUpdatedInfo("Super Admin", "newadmin@ebs.com", "Operations");
        boolean success = service.updateProfile(updated);
        assertTrue(success);

        AdminProfile retrieved = service.getAdminProfile("admin");
        assertEquals("Super Admin", retrieved.fullName());
        assertEquals("newadmin@ebs.com", retrieved.email());
        assertEquals("Operations", retrieved.department());
    }

    @Test
    @DisplayName("Should generate fallback profile for unknown admin username")
    void testFallbackAdminProfile() {
        AdminProfile fallback = service.getAdminProfile("unknown_admin");
        assertNotNull(fallback);
        assertEquals("unknown_admin", fallback.username());
        assertTrue(fallback.adminId().contains("ADM-GEN"));
    }
}
