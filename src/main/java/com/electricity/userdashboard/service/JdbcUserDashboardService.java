package com.electricity.userdashboard.service;

import com.electricity.config.DatabaseAccessException;
import com.electricity.config.DatabaseConnection;
import com.electricity.userdashboard.model.ConsumerBill;
import com.electricity.userdashboard.model.ConsumerProfile;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Reads the signed-in consumer's profile and billing records from MySQL. */
public final class JdbcUserDashboardService implements UserDashboardService {

    private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    @Override
    public ConsumerProfile getConsumerProfile(String username) {
        String sql = """
                SELECT u.username, c.name, c.consumer_id, c.address, c.phone, c.email,
                       c.meter_number, t.tariff_name, c.tariff_category
                FROM users u
                JOIN consumers c ON c.consumer_id = u.consumer_id
                JOIN tariffs t ON t.tariff_code = c.tariff_category
                WHERE u.username = ? AND u.`role` = 'USER'
                  AND u.status = 'ACTIVE' AND c.status = 'ACTIVE'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException("No active consumer profile is linked to this account.");
                }
                String contact = Stream.of(result.getString("phone"), result.getString("email"))
                        .filter(value -> value != null && !value.isBlank())
                        .collect(Collectors.joining(" | "));
                String tariffName = result.getString("tariff_name");
                String tariffCode = result.getString("tariff_category");
                return new ConsumerProfile(
                        result.getString("username"),
                        result.getString("name"),
                        result.getString("consumer_id"),
                        valueOrDash(result.getString("address")),
                        result.getString("meter_number"),
                        tariffName + " (" + tariffCode + ")",
                        valueOrDash(contact),
                        valueOrEmpty(result.getString("phone")),
                        valueOrEmpty(result.getString("email"))
                );
            }
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load the consumer profile from MySQL.", exception);
        }
    }

    @Override
    public ConsumerBill getCurrentBill(String consumerNumber) {
        String sql = billSelect() + " WHERE b.consumer_id = ? ORDER BY b.billing_period DESC, b.generated_at DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, consumerNumber);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? mapBill(result) : ConsumerBill.empty();
            }
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load the current bill from MySQL.", exception);
        }
    }

    @Override
    public List<ConsumerBill> getBillHistory(String consumerNumber) {
        String sql = billSelect() + " WHERE b.consumer_id = ? ORDER BY b.billing_period DESC, b.generated_at DESC";
        List<ConsumerBill> bills = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, consumerNumber);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    bills.add(mapBill(result));
                }
            }
            return List.copyOf(bills);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load bill history from MySQL.", exception);
        }
    }

    @Override
    public boolean updateContactDetails(String username, String address, String phone, String email) {
        String cleanPhone = phone == null || phone.isBlank() ? null : phone.trim();
        String cleanEmail = email == null || email.isBlank() ? null : email.trim();
        if (cleanPhone != null && !cleanPhone.matches("[+0-9() .-]{7,20}")) {
            throw new IllegalArgumentException("Enter a valid phone number.");
        }
        if (cleanEmail != null && !cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int updated;
                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE consumers c JOIN users u ON u.consumer_id = c.consumer_id
                        SET c.address = ?, c.phone = ?, c.email = ?
                        WHERE u.username = ? AND u.`role` = 'USER' AND u.status = 'ACTIVE'
                          AND c.status = 'ACTIVE'
                        """)) {
                    statement.setString(1, address == null || address.isBlank() ? null : address.trim());
                    statement.setString(2, cleanPhone);
                    statement.setString(3, cleanEmail);
                    statement.setString(4, username);
                    updated = statement.executeUpdate();
                }
                if (updated == 0) {
                    connection.rollback();
                    return false;
                }
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO activities (user_id, activity_type, description)
                        SELECT id, 'PROFILE_UPDATED', CONCAT('Consumer profile updated: ', username)
                        FROM users WHERE username = ? AND `role` = 'USER'
                        """)) {
                    statement.setString(1, username);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to save the consumer profile to MySQL.", exception);
        }
    }

    private static String billSelect() {
        return """
                SELECT b.bill_number, b.billing_period, b.previous_reading, b.current_reading,
                       b.units_consumed, b.energy_charge, b.fixed_charge, b.tax_charge,
                       b.total_amount,
                       GREATEST(b.total_amount - COALESCE((SELECT SUM(paid.amount) FROM payments paid
                           WHERE paid.bill_id = b.id AND paid.status = 'SUCCESS'), 0), 0) AS outstanding_amount,
                       CASE WHEN EXISTS (
                           SELECT 1 FROM payments p WHERE p.bill_id = b.id AND p.status = 'PENDING'
                       ) THEN 'PAYMENT_PENDING' ELSE b.payment_status END AS payment_status,
                       b.due_date
                FROM bills b
                """;
    }

    private static ConsumerBill mapBill(ResultSet result) throws SQLException {
        Date dueDate = result.getDate("due_date");
        return new ConsumerBill(
                result.getString("bill_number"),
                result.getString("billing_period"),
                decimalOrZero(result.getBigDecimal("previous_reading")),
                decimalOrZero(result.getBigDecimal("current_reading")),
                decimalOrZero(result.getBigDecimal("units_consumed")),
                decimalOrZero(result.getBigDecimal("energy_charge")),
                decimalOrZero(result.getBigDecimal("fixed_charge")),
                decimalOrZero(result.getBigDecimal("tax_charge")),
                decimalOrZero(result.getBigDecimal("total_amount")),
                decimalOrZero(result.getBigDecimal("outstanding_amount")),
                result.getString("payment_status"),
                dueDate == null ? "—" : DUE_DATE_FORMAT.format(dueDate.toLocalDate())
        );
    }

    private static BigDecimal decimalOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
