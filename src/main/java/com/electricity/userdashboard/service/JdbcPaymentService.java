package com.electricity.userdashboard.service;

import com.electricity.config.DatabaseAccessException;
import com.electricity.config.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.Locale;
import java.util.UUID;

/** Saves a payment request as PENDING until an actual gateway or cashier confirms settlement. */
public final class JdbcPaymentService implements PaymentService {
    private static final Set<String> PAYMENT_METHODS = Set.of("CASH", "UPI", "CARD", "BANK_TRANSFER");

    @Override
    public String recordPendingPayment(String consumerNumber, String billNumber, String paymentMethod) {
        if (consumerNumber == null || billNumber == null) {
            throw new IllegalArgumentException("A consumer and bill are required.");
        }
        String method = paymentMethod == null ? "" : paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!PAYMENT_METHODS.contains(method)) throw new IllegalArgumentException("Choose a supported payment method.");

        String transactionReference = "LOCAL-PENDING-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long billId;
                BigDecimal totalAmount;
                String status;
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT id, total_amount, payment_status
                        FROM bills
                        WHERE bill_number = ? AND consumer_id = ?
                        FOR UPDATE
                        """)) {
                    statement.setString(1, billNumber);
                    statement.setString(2, consumerNumber);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new IllegalArgumentException("The bill was not found for this consumer.");
                        billId = result.getLong("id");
                        totalAmount = result.getBigDecimal("total_amount");
                        status = result.getString("payment_status");
                    }
                }
                if ("PAID".equalsIgnoreCase(status)) throw new IllegalArgumentException("This bill is already paid.");

                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT COUNT(*) FROM payments WHERE bill_id = ? AND status = 'PENDING'")) {
                    statement.setLong(1, billId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next() && result.getInt(1) > 0) {
                            throw new IllegalArgumentException("A payment request for this bill is already pending.");
                        }
                    }
                }

                BigDecimal paidAmount;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE bill_id = ? AND status = 'SUCCESS'")) {
                    statement.setLong(1, billId);
                    try (ResultSet result = statement.executeQuery()) {
                        paidAmount = result.next() ? result.getBigDecimal(1) : BigDecimal.ZERO;
                    }
                }
                BigDecimal amountDue = totalAmount.subtract(paidAmount);
                if (amountDue.signum() <= 0) throw new IllegalArgumentException("There is no outstanding amount for this bill.");

                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO payments
                            (bill_id, consumer_id, amount, payment_method, transaction_reference, payment_date, status)
                        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'PENDING')
                        """)) {
                    statement.setLong(1, billId);
                    statement.setString(2, consumerNumber);
                    statement.setBigDecimal(3, amountDue);
                    statement.setString(4, method);
                    statement.setString(5, transactionReference);
                    statement.executeUpdate();
                }
                connection.commit();
                return transactionReference;
            } catch (IllegalArgumentException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to record the payment request in MySQL.", exception);
        }
    }
}
