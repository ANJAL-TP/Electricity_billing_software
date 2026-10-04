package com.electricity.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens JDBC connections to the configured MySQL database.
 * Callers own the returned connection and should close it with try-with-resources.
 */
public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        DatabaseConfig config = DatabaseConfig.load();
        try {
            return DriverManager.getConnection(config.url(), config.username(), config.password());
        } catch (SQLException exception) {
            throw new SQLException(
                    "Could not connect to MySQL. Check that MySQL is running, schema.sql has been imported, " +
                            "and DB_URL, DB_USER, and DB_PASSWORD are configured.",
                    exception
            );
        }
    }

    /** Simple standalone connection check: run this class with the configured database settings. */
    public static void main(String[] args) throws SQLException {
        try (Connection connection = getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            System.out.printf(
                    "Connected to %s %s.%n",
                    metadata.getDatabaseProductName(),
                    metadata.getDatabaseProductVersion()
            );
        }
    }
}
