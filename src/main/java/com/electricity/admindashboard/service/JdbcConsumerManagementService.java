package com.electricity.admindashboard.service;

import com.electricity.admindashboard.model.ManagedConsumer;
import com.electricity.config.DatabaseAccessException;
import com.electricity.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** MySQL-backed consumer search, create, update, and soft-deactivation. */
public final class JdbcConsumerManagementService implements ConsumerManagementService {
    private final String actorUsername;

    public JdbcConsumerManagementService(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    @Override
    public List<ManagedConsumer> search(String query) {
        String sql = """
                SELECT consumer_id, name, address, phone, email, meter_number, tariff_category, status
                FROM consumers
                WHERE (? = '' OR consumer_id LIKE ? OR name LIKE ? OR address LIKE ?
                       OR phone LIKE ? OR email LIKE ? OR meter_number LIKE ? OR tariff_category LIKE ?)
                ORDER BY consumer_id
                """;
        String value = query == null ? "" : query.trim();
        String pattern = "%" + value + "%";
        List<ManagedConsumer> consumers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            for (int i = 2; i <= 8; i++) statement.setString(i, pattern);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    consumers.add(new ManagedConsumer(
                            result.getString("consumer_id"), result.getString("name"),
                            valueOrEmpty(result.getString("address")), valueOrEmpty(result.getString("phone")),
                            valueOrEmpty(result.getString("email")), result.getString("meter_number"),
                            result.getString("tariff_category"), result.getString("status")
                    ));
                }
            }
            return List.copyOf(consumers);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to search consumers in MySQL.", exception);
        }
    }

    @Override
    public List<String> getTariffCodes() {
        String sql = "SELECT tariff_code FROM tariffs WHERE active = TRUE ORDER BY tariff_code";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<String> codes = new ArrayList<>();
            while (result.next()) codes.add(result.getString(1));
            return List.copyOf(codes);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load active tariffs from MySQL.", exception);
        }
    }

    @Override
    public void create(ManagedConsumer consumer) {
        validate(consumer);
        String sql = """
                INSERT INTO consumers
                    (consumer_id, name, address, phone, email, meter_number, tariff_category, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
                """;
        inTransaction((connection) -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindConsumer(statement, consumer);
                statement.executeUpdate();
            }
            addActivity(connection, "CONSUMER_CREATED", "Consumer created: " + consumer.consumerId());
        }, "Consumer ID or meter number already exists.", "Unable to create the consumer in MySQL.");
    }

    @Override
    public void update(String originalConsumerId, ManagedConsumer consumer) {
        validate(consumer);
        String sql = """
                UPDATE consumers
                SET consumer_id = ?, name = ?, address = ?, phone = ?, email = ?,
                    meter_number = ?, tariff_category = ?
                WHERE consumer_id = ?
                """;
        inTransaction((connection) -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindConsumer(statement, consumer);
                statement.setString(8, originalConsumerId);
                if (statement.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Consumer record was not found.");
                }
            }
            addActivity(connection, "CONSUMER_UPDATED", "Consumer updated: " + consumer.consumerId());
        }, "Consumer ID or meter number already exists.", "Unable to update the consumer in MySQL.");
    }

    @Override
    public void setActive(String consumerId, boolean active) {
        String activityType = active ? "CONSUMER_UPDATED" : "CONSUMER_DEACTIVATED";
        String status = active ? "ACTIVE" : "INACTIVE";
        inTransaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE consumers SET status = ? WHERE consumer_id = ?")) {
                statement.setString(1, status);
                statement.setString(2, consumerId);
                if (statement.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Consumer record was not found.");
                }
            }
            addActivity(connection, activityType,
                    (active ? "Consumer activated: " : "Consumer deactivated: ") + consumerId);
        }, "", "Unable to change consumer status in MySQL.");
    }

    private void inTransaction(SqlWork work, String duplicateMessage, String databaseMessage) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                work.run(connection);
                connection.commit();
            } catch (IllegalArgumentException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                if ("23000".equals(exception.getSQLState()) && !duplicateMessage.isBlank()) {
                    throw new IllegalArgumentException(duplicateMessage, exception);
                }
                throw exception;
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw new DatabaseAccessException(databaseMessage, exception);
        }
    }

    private void addActivity(Connection connection, String type, String description) throws SQLException {
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
                "INSERT INTO activities (user_id, activity_type, description) VALUES (?, ?, ?)")) {
            if (userId == null) statement.setNull(1, java.sql.Types.BIGINT);
            else statement.setLong(1, userId);
            statement.setString(2, type);
            statement.setString(3, description);
            statement.executeUpdate();
        }
    }

    private static void bindConsumer(PreparedStatement statement, ManagedConsumer consumer) throws SQLException {
        statement.setString(1, consumer.consumerId());
        statement.setString(2, consumer.name());
        statement.setString(3, emptyToNull(consumer.address()));
        statement.setString(4, emptyToNull(consumer.phone()));
        statement.setString(5, emptyToNull(consumer.email()));
        statement.setString(6, consumer.meterNumber());
        statement.setString(7, consumer.tariffCode());
    }

    private static void validate(ManagedConsumer consumer) {
        if (consumer == null || blank(consumer.consumerId()) || blank(consumer.name()) || blank(consumer.meterNumber())) {
            throw new IllegalArgumentException("Consumer ID, name, and meter number are required.");
        }
        if (!blank(consumer.phone()) && !consumer.phone().trim().matches("[+0-9() .-]{7,20}")) {
            throw new IllegalArgumentException("Enter a valid phone number.");
        }
        if (!blank(consumer.email()) && !consumer.email().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (blank(consumer.tariffCode())) throw new IllegalArgumentException("Choose an active tariff.");
    }

    private static String valueOrEmpty(String value) { return value == null ? "" : value; }
    private static String emptyToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static boolean blank(String value) { return value == null || value.isBlank(); }

    @FunctionalInterface
    private interface SqlWork { void run(Connection connection) throws SQLException; }
}
