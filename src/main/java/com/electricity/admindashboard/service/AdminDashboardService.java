package com.electricity.admindashboard.service;

import com.electricity.admindashboard.model.AdminProfile;
import com.electricity.admindashboard.model.DashboardStats;
import com.electricity.admindashboard.model.RecentActivity;

import java.util.List;

/**
 * Service contract for fetching and updating administrative dashboard data.
 * Keeps the Swing UI cleanly separated from the underlying persistence layer (JDBC/MySQL).
 */
public interface AdminDashboardService {

    /**
     * Retrieves high-level billing and consumer statistics for the current cycle.
     */
    DashboardStats getDashboardStats();

    /**
     * Retrieves the most recent system activities (registrations, bills, payments, readings).
     */
    List<RecentActivity> getRecentActivities();

    /**
     * Retrieves administrator profile details by username.
     */
    AdminProfile getAdminProfile(String username);

    /**
     * Updates an administrator's profile details.
     */
    boolean updateProfile(AdminProfile profile);
}
