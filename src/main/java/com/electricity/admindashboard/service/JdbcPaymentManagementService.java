package com.electricity.admindashboard.service;

import com.electricity.config.DatabaseAccessException;
import com.electricity.config.DatabaseConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Records confirmed payments, settles consumer requests, and updates bill status atomically. */
public final class JdbcPaymentManagementService implements PaymentManagementService {
    private static final Set<String> METHODS = Set.of("CASH", "UPI", "CARD", "BANK_TRANSFER");
    private final String actorUsername;

    public JdbcPaymentManagementService(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    @Override
    public Optional<PaymentBill> findBill(String billNumber) {
        if (billNumber == null || billNumber.isBlank()) return Optional.empty();
        try (Connection connection = DatabaseConnection.getConnection()) {
            return loadBill(connection, billNumber.trim(), false);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to find the bill in MySQL.", exception);
        }
    }

    @Override
    public List<PaymentRecord> getRecentPayments() {
        String sql = """
                SELECT b.bill_number, p.consumer_id, c.name, p.amount, p.payment_method,
                       p.transaction_reference, p.status, p.payment_date
                FROM payments p JOIN bills b ON b.id = p.bill_id
                JOIN consumers c ON c.consumer_id = p.consumer_id
                ORDER BY p.created_at DESC, p.id DESC LIMIT 100
                """;
        List<PaymentRecord> records = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Timestamp date = result.getTimestamp("payment_date");
                records.add(new PaymentRecord(
                        result.getString("bill_number"), result.getString("consumer_id"),
                        result.getString("name"), result.getBigDecimal("amount"),
                        result.getString("payment_method"), result.getString("transaction_reference"),
                        result.getString("status"), date == null ? "" : date.toLocalDateTime().toString().replace('T', ' ')
                ));
            }
            return List.copyOf(records);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load payment history from MySQL.", exception);
        }
    }

    @Override
    public PaymentBill recordPayment(String billNumber, BigDecimal amount, String paymentMethod, String transactionReference) {
        if (billNumber == null || billNumber.isBlank()) throw new IllegalArgumentException("Enter a bill number.");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Payment amount must be greater than zero.");
        if (amount.stripTrailingZeros().scale() > 2) throw new IllegalArgumentException("Payment amount can have at most two decimal places.");
        BigDecimal paymentAmount = amount.setScale(2, RoundingMode.UNNECESSARY);
        String method = paymentMethod == null ? "" : paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!METHODS.contains(method)) throw new IllegalArgumentException("Choose a supported payment method.");
        String reference = transactionReference == null || transactionReference.isBlank() ? null : transactionReference.trim();

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                PaymentBill bill = loadBill(connection, billNumber.trim(), true)
                        .orElseThrow(() -> new IllegalArgumentException("The bill number was not found."));
                if (bill.outstandingAmount().signum() <= 0) throw new IllegalArgumentException("This bill has no outstanding balance.");
                if (paymentAmount.compareTo(bill.outstandingAmount()) > 0) {
                    throw new IllegalArgumentException("Payment cannot exceed the outstanding amount of " + bill.outstandingAmount() + ".");
                }

                Long pendingId = findPendingRequestId(connection, billNumber.trim());
                if (pendingId == null) {
                    insertSuccessfulPayment(connection, bill, paymentAmount, method, reference);
                } else {
                    settlePendingRequest(connection, pendingId, paymentAmount, method, reference);
                }

                BigDecimal newPaid = bill.paidAmount().add(paymentAmount);
                String newStatus = newPaid.compareTo(bill.totalAmount()) >= 0 ? "PAID" : "PARTIALLY_PAID";
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE bills SET payment_status = ? WHERE bill_number = ?")) {
                    update.setString(1, newStatus);
                    update.setString(2, bill.billNumber());
                    update.executeUpdate();
                }
                addActivity(connection, "Payment received for " + bill.billNumber() + " (" + paymentAmount + ")");
                connection.commit();
                return loadBill(connection, bill.billNumber(), false).orElseThrow();
            } catch (IllegalArgumentException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                if ("23000".equals(exception.getSQLState())) {
                    throw new IllegalArgumentException("That transaction reference is already in use.", exception);
                }
                throw exception;
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to record the payment in MySQL.", exception);
        }
    }

    private static Optional<PaymentBill> loadBill(Connection connection, String billNumber, boolean lock) throws SQLException {
        String sql = """
                SELECT b.id AS bill_id, b.bill_number, b.consumer_id, c.name,
                       b.billing_period, b.total_amount, b.payment_status
                FROM bills b JOIN consumers c ON c.consumer_id = b.consumer_id
                WHERE b.bill_number = ?
                """ + (lock ? " FOR UPDATE" : "");
        long billId;
        String consumerId;
        String consumerName;
        String period;
        String status;
        BigDecimal total;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, billNumber);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                billId = result.getLong("bill_id");
                consumerId = result.getString("consumer_id");
                consumerName = result.getString("name");
                period = result.getString("billing_period");
                total = result.getBigDecimal("total_amount");
                status = result.getString("payment_status");
            }
        }
        BigDecimal paid;
        try (PreparedStatement paidQuery = connection.prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE bill_id = ? AND status = 'SUCCESS'")) {
            paidQuery.setLong(1, billId);
            try (ResultSet paidResult = paidQuery.executeQuery()) {
                paid = paidResult.next() ? paidResult.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
        BigDecimal due = total.subtract(paid).max(BigDecimal.ZERO);
        return Optional.of(new PaymentBill(billNumber, consumerId, consumerName, period, total, paid, due, status));
    }

    private static Long findPendingRequestId(Connection connection, String billNumber) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT p.id FROM payments p JOIN bills b ON b.id = p.bill_id
                WHERE b.bill_number = ? AND p.status = 'PENDING'
                ORDER BY p.created_at, p.id LIMIT 1 FOR UPDATE
                """)) {
            statement.setString(1, billNumber);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : null;
            }
        }
    }

    private static void insertSuccessfulPayment(Connection connection, PaymentBill bill, BigDecimal amount,
                                                String method, String reference) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO payments (bill_id, consumer_id, amount, payment_method, transaction_reference, payment_date, status)
                SELECT id, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'SUCCESS' FROM bills WHERE bill_number = ?
                """)) {
            statement.setString(1, bill.consumerId());
            statement.setBigDecimal(2, amount);
            statement.setString(3, method);
            statement.setString(4, reference);
            statement.setString(5, bill.billNumber());
            statement.executeUpdate();
        }
    }

    private static void settlePendingRequest(Connection connection, long paymentId, BigDecimal amount,
                                             String method, String reference) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE payments SET amount = ?, payment_method = ?,
                       transaction_reference = COALESCE(?, transaction_reference),
                       payment_date = CURRENT_TIMESTAMP, status = 'SUCCESS'
                WHERE id = ? AND status = 'PENDING'
                """)) {
            statement.setBigDecimal(1, amount);
            statement.setString(2, method);
            statement.setString(3, reference);
            statement.setLong(4, paymentId);
            if (statement.executeUpdate() != 1) throw new SQLException("The pending request changed before it could be confirmed.");
        }
    }

    private void addActivity(Connection connection, String description) throws SQLException {
        Long userId = null;
        if (actorUsername != null && !actorUsername.isBlank()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM users WHERE username = ? AND `role` = 'ADMIN'")) {
                statement.setString(1, actorUsername);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) userId = result.getLong(1);
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO activities (user_id, activity_type, description) VALUES (?, 'PAYMENT_RECEIVED', ?)")) {
            if (userId == null) statement.setNull(1, java.sql.Types.BIGINT);
            else statement.setLong(1, userId);
            statement.setString(2, description);
            statement.executeUpdate();
        }
    }
}
