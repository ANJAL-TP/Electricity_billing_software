package com.electricity.admindashboard.service;

import com.electricity.billgeneration.model.BillCalculationResult;
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

/** Reads saved bills and consumer details from MySQL for admin history. */
public final class JdbcBillHistoryService implements BillHistoryService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public List<String> getBillingPeriods() {
        List<String> periods = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT DISTINCT billing_period FROM bills ORDER BY billing_period DESC");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) periods.add(result.getString(1));
            return List.copyOf(periods);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load bill periods from MySQL.", exception);
        }
    }

    @Override
    public List<BillHistoryRecord> search(String query, String billingPeriod, String paymentStatus) {
        String sql = """
                SELECT b.bill_number, b.billing_period, b.consumer_id, c.name AS consumer_name,
                       c.address, c.meter_number, b.tariff_category, b.previous_reading,
                       b.current_reading, b.units_consumed, b.energy_charge, b.fixed_charge,
                       b.tax_charge, b.total_amount,
                       CASE WHEN EXISTS (SELECT 1 FROM payments p WHERE p.bill_id = b.id AND p.status = 'PENDING')
                            THEN 'PAYMENT_PENDING' ELSE b.payment_status END AS payment_status,
                       b.generated_at, b.due_date
                FROM bills b JOIN consumers c ON c.consumer_id = b.consumer_id
                WHERE (? = '' OR b.bill_number LIKE ? OR b.consumer_id LIKE ? OR c.name LIKE ?)
                  AND (? = 'ALL' OR b.billing_period = ?)
                  AND (? = 'ALL' OR
                       CASE WHEN EXISTS (SELECT 1 FROM payments p WHERE p.bill_id = b.id AND p.status = 'PENDING')
                            THEN 'PAYMENT_PENDING' ELSE b.payment_status END = ?)
                ORDER BY b.billing_period DESC, b.generated_at DESC
                LIMIT 1000
                """;
        String value = query == null ? "" : query.trim();
        String pattern = "%" + value + "%";
        String period = billingPeriod == null ? "ALL" : billingPeriod;
        String status = paymentStatus == null ? "ALL" : paymentStatus;
        List<BillHistoryRecord> records = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, period);
            statement.setString(6, period);
            statement.setString(7, status);
            statement.setString(8, status);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    double previous = result.getBigDecimal("previous_reading").doubleValue();
                    double current = result.getBigDecimal("current_reading").doubleValue();
                    double units = result.getBigDecimal("units_consumed").doubleValue();
                    String name = result.getString("consumer_name");
                    BillCalculationResult bill = new BillCalculationResult(
                            result.getString("bill_number"), result.getString("billing_period"),
                            result.getString("consumer_id"), name,
                            valueOrEmpty(result.getString("address")), result.getString("meter_number"),
                            result.getString("tariff_category"), previous, current, units,
                            result.getBigDecimal("energy_charge").doubleValue(),
                            result.getBigDecimal("fixed_charge").doubleValue(),
                            result.getBigDecimal("tax_charge").doubleValue(),
                            result.getBigDecimal("total_amount").doubleValue()
                    );
                    Timestamp generated = result.getTimestamp("generated_at");
                    java.sql.Date due = result.getDate("due_date");
                    records.add(new BillHistoryRecord(
                            bill, name,
                            generated == null ? "" : DATE_TIME.format(generated.toLocalDateTime()),
                            due == null ? "" : due.toLocalDate().toString(),
                            result.getString("payment_status")
                    ));
                }
            }
            return List.copyOf(records);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to search bill history in MySQL.", exception);
        }
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
