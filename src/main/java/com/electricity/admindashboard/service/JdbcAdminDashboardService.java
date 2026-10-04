package com.electricity.admindashboard.service;

import com.electricity.admindashboard.model.AdminProfile;
import com.electricity.admindashboard.model.DashboardStats;
import com.electricity.admindashboard.model.RecentActivity;
import com.electricity.config.DatabaseAccessException;
import com.electricity.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Loads administrator dashboard metrics, profile data, and activity from MySQL. */
public final class JdbcAdminDashboardService implements AdminDashboardService {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.ENGLISH);
    private static final String DEFAULT_DEPARTMENT = "Electricity Billing Administration";

    @Override
    public DashboardStats getDashboardStats() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int totalConsumers;
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM consumers WHERE status = 'ACTIVE'" );
                 ResultSet result = statement.executeQuery()) {
                if (result.next()) totalConsumers = result.getInt(1);
                else totalConsumers = 0;
            }
            String totalsSql = """
                    SELECT COUNT(*) AS total_bills,
                           COALESCE(SUM(payment_status = 'PAID'), 0) AS paid_bills,
                           COALESCE(SUM(payment_status IN ('UNPAID', 'PARTIALLY_PAID', 'OVERDUE')), 0) AS unpaid_bills
                    FROM bills
                    """;
            int totalBills;
            int paidBills;
            int unpaidBills;
            try (PreparedStatement statement = connection.prepareStatement(totalsSql);
                 ResultSet result = statement.executeQuery()) {
                result.next();
                totalBills = result.getInt("total_bills");
                paidBills = result.getInt("paid_bills");
                unpaidBills = result.getInt("unpaid_bills");
            }
            double revenue;
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'SUCCESS'");
                 ResultSet result = statement.executeQuery()) {
                result.next();
                revenue = result.getBigDecimal(1).doubleValue();
            }
            return new DashboardStats(totalConsumers, totalBills, paidBills, unpaidBills, revenue);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load administrator dashboard statistics from MySQL.", exception);
        }
    }

    @Override
    public List<RecentActivity> getRecentActivities() {
        String sql = """
                SELECT activity_type, description, created_at
                FROM activities
                ORDER BY created_at DESC, id DESC
                LIMIT 8
                """;
        List<RecentActivity> activities = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                String activityType = result.getString("activity_type");
                String description = result.getString("description");
                Timestamp createdAt = result.getTimestamp("created_at");
                activities.add(new RecentActivity(
                        createdAt == null ? "—" : DATE_TIME_FORMAT.format(createdAt.toLocalDateTime()),
                        categoryFor(activityType),
                        titleFor(activityType),
                        description,
                        "Completed"
                ));
            }
            return List.copyOf(activities);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load recent activities from MySQL.", exception);
        }
    }

    @Override
    public AdminProfile getAdminProfile(String username) {
        String sql = """
                SELECT id, username, full_name, email, last_login
                FROM users
                WHERE username = ? AND `role` = 'ADMIN' AND status = 'ACTIVE'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException("The administrator profile was not found.");
                }
                long userId = result.getLong("id");
                String adminId = String.format(Locale.ROOT, "ADM-%06d", userId);
                Timestamp lastLogin = result.getTimestamp("last_login");
                return new AdminProfile(
                        adminId,
                        result.getString("username"),
                        valueOrDefault(result.getString("full_name"), result.getString("username")),
                        valueOrDefault(result.getString("email"), ""),
                        DEFAULT_DEPARTMENT,
                        lastLogin == null ? "—" : DATE_TIME_FORMAT.format(lastLogin.toLocalDateTime())
                );
            }
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load the administrator profile from MySQL.", exception);
        }
    }

    @Override
    public boolean updateProfile(AdminProfile profile) {
        if (profile == null || profile.username() == null) return false;
        String sql = "UPDATE users SET full_name = ?, email = ? WHERE username = ? AND `role` = 'ADMIN'";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long userId;
                try (PreparedStatement findUser = connection.prepareStatement(
                        "SELECT id FROM users WHERE username = ? AND `role` = 'ADMIN'")) {
                    findUser.setString(1, profile.username());
                    try (ResultSet result = findUser.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                        userId = result.getLong(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, profile.fullName());
                    statement.setString(2, profile.email());
                    statement.setString(3, profile.username());
                    statement.executeUpdate();
                }
                try (PreparedStatement activity = connection.prepareStatement(
                        "INSERT INTO activities (user_id, activity_type, description) VALUES (?, 'PROFILE_UPDATED', ?)")) {
                    activity.setLong(1, userId);
                    activity.setString(2, "Administrator profile updated: " + profile.username());
                    activity.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to save the administrator profile to MySQL.", exception);
        }
    }

    private static String categoryFor(String activityType) {
        return switch (activityType) {
            case "CONSUMER_CREATED", "CONSUMER_UPDATED", "CONSUMER_DEACTIVATED" -> "Consumer";
            case "BILL_GENERATED" -> "Billing";
            case "PAYMENT_RECEIVED" -> "Payment";
            case "METER_READING_ADDED" -> "Meter";
            case "LOGIN", "PROFILE_UPDATED" -> "Account";
            default -> "System";
        };
    }

    private static String titleFor(String activityType) {
        return switch (activityType) {
            case "CONSUMER_CREATED" -> "Consumer created";
            case "CONSUMER_UPDATED" -> "Consumer updated";
            case "CONSUMER_DEACTIVATED" -> "Consumer deactivated";
            case "BILL_GENERATED" -> "Bill generated";
            case "PAYMENT_RECEIVED" -> "Payment received";
            case "METER_READING_ADDED" -> "Meter reading added";
            case "LOGIN" -> "User login";
            case "PROFILE_UPDATED" -> "Profile updated";
            default -> activityType;
        };
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
