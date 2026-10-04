package com.electricity.admindashboard.model;

/**
 * Immutable record representing an audit / activity log entry in the system.
 *
 * @param timestamp    formatted date/time string of the event
 * @param activityType type category (e.g., "Consumer", "Billing", "Payment", "Meter")
 * @param title        short summary header
 * @param description  detailed context or reference IDs
 * @param status       event status (e.g., "Completed", "Pending", "Verified")
 */
public record RecentActivity(
        String timestamp,
        String activityType,
        String title,
        String description,
        String status
) {
}
