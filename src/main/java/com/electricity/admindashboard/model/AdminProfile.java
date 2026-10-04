package com.electricity.admindashboard.model;

/**
 * Immutable record representing an administrator's profile information.
 *
 * @param adminId    unique administrator identifier (e.g. ADM-2024-001)
 * @param username   login username
 * @param fullName   full name of the administrator
 * @param email      official contact email
 * @param department department / unit within the electricity board
 * @param lastLogin  formatted timestamp of the last login session
 */
public record AdminProfile(
        String adminId,
        String username,
        String fullName,
        String email,
        String department,
        String lastLogin
) {
    public AdminProfile withUpdatedInfo(String fullName, String email, String department) {
        return new AdminProfile(this.adminId, this.username, fullName, email, department, this.lastLogin);
    }
}
