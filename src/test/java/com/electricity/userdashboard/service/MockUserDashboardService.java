package com.electricity.userdashboard.service;

import com.electricity.userdashboard.model.ConsumerBill;
import com.electricity.userdashboard.model.ConsumerProfile;

import java.math.BigDecimal;
import java.util.List;

/** Sample consumer data for frontend development without a database connection. */
public class MockUserDashboardService implements UserDashboardService {

    @Override
    public ConsumerProfile getConsumerProfile(String username) {
        if ("john_doe".equalsIgnoreCase(username)) {
            return new ConsumerProfile(
                    "john_doe", "John Doe", "CON-2024-0101",
                    "24 Green Park Avenue, Springfield", "MTR-778812",
                    "Domestic - Residential", "+1 555 014 2024 | john.doe@example.com"
            );
        }

        return new ConsumerProfile(
                username == null || username.isBlank() ? "user" : username,
                "Alice Johnson", "CON-2024-0042",
                "18 Lake View Road, Springfield", "MTR-445566",
                "Domestic - Residential", "+1 555 010 4242 | alice.johnson@example.com"
        );
    }

    @Override
    public ConsumerBill getCurrentBill(String consumerNumber) {
        return new ConsumerBill(
                "BILL-2025-09-0042", "August 2025",
                amount("12480"), amount("12642"), amount("162"),
                amount("1458.00"), amount("120.00"), amount("157.80"), amount("1735.80"),
                "Pending", "15 September 2025"
        );
    }

    @Override
    public List<ConsumerBill> getBillHistory(String consumerNumber) {
        return List.of(
                getCurrentBill(consumerNumber),
                new ConsumerBill("BILL-2025-08-0042", "July 2025", amount("12310"), amount("12480"), amount("170"), amount("1530.00"), amount("120.00"), amount("165.00"), amount("1815.00"), "Paid", "15 August 2025"),
                new ConsumerBill("BILL-2025-07-0042", "June 2025", amount("12142"), amount("12310"), amount("168"), amount("1512.00"), amount("120.00"), amount("163.20"), amount("1795.20"), "Paid", "15 July 2025"),
                new ConsumerBill("BILL-2025-06-0042", "May 2025", amount("11980"), amount("12142"), amount("162"), amount("1458.00"), amount("120.00"), amount("157.80"), amount("1735.80"), "Paid", "15 June 2025")
        );
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }
}
