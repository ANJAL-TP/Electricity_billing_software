package com.electricity.admindashboard.service;

import com.electricity.admindashboard.model.AdminProfile;
import com.electricity.admindashboard.model.DashboardStats;
import com.electricity.admindashboard.model.RecentActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock implementation of {@link AdminDashboardService} providing realistic sample data
 * for college demonstration. Another team member can implement a JDBC/MySQL version of
 * this interface without changing any Swing UI classes.
 */
public class MockAdminDashboardService implements AdminDashboardService {

    private DashboardStats currentStats;
    private final List<RecentActivity> recentActivities = new ArrayList<>();
    private final Map<String, AdminProfile> adminProfiles = new ConcurrentHashMap<>();

    public MockAdminDashboardService() {
        seedInitialData();
    }

    private void seedInitialData() {
        // 1. Initial Billing & Consumer Metrics
        currentStats = new DashboardStats(
                12450,      // Total Consumers
                8920,       // Total Bills
                7640,       // Paid Bills
                1280,       // Unpaid Bills
                458920.00   // Total Revenue ($)
        );

        // 2. Recent System Activities
        recentActivities.add(new RecentActivity(
                "Today, 10:14 AM",
                "Consumer",
                "New consumer registered",
                "Consumer #EBS-9024 (Rajesh Kumar) registered under Domestic 2KW tariff.",
                "Completed"
        ));
        recentActivities.add(new RecentActivity(
                "Today, 09:30 AM",
                "Billing",
                "Bill generated",
                "Cycle #09/2024 generated for 850 consumers in Feeder Zone B.",
                "Completed"
        ));
        recentActivities.add(new RecentActivity(
                "Yesterday, 04:45 PM",
                "Payment",
                "Payment received",
                "Online payment of Rs.145.50 received for Account #EBS-1082.",
                "Verified"
        ));
        recentActivities.add(new RecentActivity(
                "Yesterday, 02:15 PM",
                "Meter",
                "Meter reading updated",
                "Meter #MTR-8842 reading logged (5,420 kWh) by Field Officer Dave.",
                "Verified"
        ));
        recentActivities.add(new RecentActivity(
                "Sep 18, 11:20 AM",
                "Billing",
                "Bill generated",
                "Commercial tariff recalculation finalized for Metro Mall Complex.",
                "Completed"
        ));
        recentActivities.add(new RecentActivity(
                "Sep 18, 09:05 AM",
                "Consumer",
                "New consumer registered",
                "New High-Tension connection #EBS-9023 approved for Apex Industries.",
                "Completed"
        ));

        // 3. Administrator Profiles
        String loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a"));
        adminProfiles.put("admin", new AdminProfile(
                "ADM-2024-001",
                "admin",
                "System Administrator",
                "admin@ebs.com",
                "Central Grid & Revenue Operations",
                loginTime
        ));
        adminProfiles.put("manager", new AdminProfile(
                "ADM-2024-002",
                "manager",
                "Billing Operations Manager",
                "manager@ebs.com",
                "Regional Consumer Accounts Division",
                loginTime
        ));
    }

    @Override
    public DashboardStats getDashboardStats() {
        return currentStats;
    }

    @Override
    public List<RecentActivity> getRecentActivities() {
        return new ArrayList<>(recentActivities);
    }

    @Override
    public AdminProfile getAdminProfile(String username) {
        if (username == null || username.trim().isEmpty()) {
            username = "admin";
        }
        AdminProfile profile = adminProfiles.get(username.trim().toLowerCase());
        if (profile == null) {
            String loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a"));
            profile = new AdminProfile(
                    "ADM-GEN-999",
                    username,
                    "Administrator (" + username + ")",
                    username + "@ebs.com",
                    "General Administration",
                    loginTime
            );
            adminProfiles.put(username.toLowerCase(), profile);
        }
        return profile;
    }

    @Override
    public boolean updateProfile(AdminProfile updatedProfile) {
        if (updatedProfile == null || updatedProfile.username() == null) {
            return false;
        }
        adminProfiles.put(updatedProfile.username().toLowerCase(), updatedProfile);
        return true;
    }
}
