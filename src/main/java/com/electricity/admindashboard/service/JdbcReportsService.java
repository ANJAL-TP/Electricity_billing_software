package com.electricity.admindashboard.service;

import com.electricity.config.DatabaseAccessException;
import com.electricity.config.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Aggregate consumer, billing, payment, and monthly revenue reports from MySQL. */
public final class JdbcReportsService implements ReportsService {
    @Override
    public ReportsSummary loadReports() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            int[] consumers = consumerCounts(connection);
            Object[] billing = billingTotals(connection);
            List<MonthlyRevenue> monthly = monthlyRevenue(connection);
            List<PaymentMethodSummary> methods = paymentMethods(connection);
            return new ReportsSummary(consumers[0], consumers[1], consumers[2], (Integer) billing[0],
                    (BigDecimal) billing[1], (BigDecimal) billing[2], (BigDecimal) billing[3],
                    (Integer) billing[4], monthly, methods);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load reports from MySQL.", exception);
        }
    }

    private static int[] consumerCounts(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*) AS total, COALESCE(SUM(status = 'ACTIVE'), 0) AS active_count,
                       COALESCE(SUM(status = 'INACTIVE'), 0) AS inactive_count
                FROM consumers
                """); ResultSet result = statement.executeQuery()) {
            result.next();
            return new int[]{result.getInt("total"), result.getInt("active_count"), result.getInt("inactive_count")};
        }
    }

    private static Object[] billingTotals(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*) AS bills, COALESCE(SUM(b.total_amount), 0) AS billed,
                       COALESCE(SUM(p.paid_amount), 0) AS paid,
                       COALESCE(SUM(GREATEST(b.total_amount - p.paid_amount, 0)), 0) AS outstanding,
                       (SELECT COUNT(*) FROM payments) AS payments
                FROM bills b
                LEFT JOIN (SELECT bill_id, SUM(amount) AS paid_amount FROM payments
                           WHERE status = 'SUCCESS' GROUP BY bill_id) p ON p.bill_id = b.id
                """); ResultSet result = statement.executeQuery()) {
            result.next();
            return new Object[]{result.getInt("bills"), result.getBigDecimal("billed"),
                    result.getBigDecimal("paid"), result.getBigDecimal("outstanding"), result.getInt("payments")};
        }
    }

    private static List<MonthlyRevenue> monthlyRevenue(Connection connection) throws SQLException {
        List<MonthlyRevenue> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT b.billing_period, COUNT(*) AS bill_count, SUM(b.total_amount) AS billed,
                       SUM(COALESCE(p.paid_amount, 0)) AS paid,
                       SUM(GREATEST(b.total_amount - COALESCE(p.paid_amount, 0), 0)) AS outstanding
                FROM bills b
                LEFT JOIN (SELECT bill_id, SUM(amount) AS paid_amount FROM payments
                           WHERE status = 'SUCCESS' GROUP BY bill_id) p ON p.bill_id = b.id
                GROUP BY b.billing_period ORDER BY b.billing_period DESC LIMIT 36
                """); ResultSet result = statement.executeQuery()) {
            while (result.next()) rows.add(new MonthlyRevenue(result.getString("billing_period"),
                    result.getInt("bill_count"), result.getBigDecimal("billed"),
                    result.getBigDecimal("paid"), result.getBigDecimal("outstanding")));
        }
        return List.copyOf(rows);
    }

    private static List<PaymentMethodSummary> paymentMethods(Connection connection) throws SQLException {
        List<PaymentMethodSummary> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT payment_method, COUNT(*) AS payment_count, COALESCE(SUM(amount), 0) AS paid_amount
                FROM payments WHERE status = 'SUCCESS' GROUP BY payment_method ORDER BY payment_method
                """); ResultSet result = statement.executeQuery()) {
            while (result.next()) rows.add(new PaymentMethodSummary(result.getString("payment_method"),
                    result.getInt("payment_count"), result.getBigDecimal("paid_amount")));
        }
        return List.copyOf(rows);
    }
}
