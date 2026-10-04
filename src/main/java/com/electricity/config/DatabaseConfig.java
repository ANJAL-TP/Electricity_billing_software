package com.electricity.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Loads MySQL connection settings from process environment variables, with
 * optional values in a local .env file. Environment variables take precedence.
 */
public record DatabaseConfig(String url, String username, String password) {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/electricity_billing?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8&connectTimeout=5000&socketTimeout=10000";
    private static final String DEFAULT_USERNAME = "root";
    private static final Set<String> SUPPORTED_KEYS = Set.of("DB_URL", "DB_USER", "DB_PASSWORD");

    public DatabaseConfig {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("DB_URL must not be empty.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("DB_USER must not be empty.");
        }
        password = password == null ? "" : password;
    }

    /**
     * Loads settings from environment variables and then the working
     * directory's optional .env file. The environment takes precedence.
     */
    public static DatabaseConfig load() {
        Map<String, String> localSettings = readDotEnv(Path.of(".env"));
        Map<String, String> environment = System.getenv();

        String url = setting("DB_URL", DEFAULT_URL, environment, localSettings);
        String username = setting("DB_USER", DEFAULT_USERNAME, environment, localSettings);
        String password = setting("DB_PASSWORD", "", environment, localSettings);
        return new DatabaseConfig(url, username, password);
    }

    private static String setting(
            String key,
            String defaultValue,
            Map<String, String> environment,
            Map<String, String> localSettings
    ) {
        String value = environment.get(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        value = localSettings.get(key);
        return value == null ? defaultValue : value;
    }

    private static Map<String, String> readDotEnv(Path path) {
        if (!Files.isRegularFile(path)) {
            return Map.of();
        }

        Map<String, String> settings = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, separator).trim();
                if (!SUPPORTED_KEYS.contains(key)) {
                    continue;
                }

                String value = trimmed.substring(separator + 1).trim();
                if (value.length() >= 2) {
                    char first = value.charAt(0);
                    char last = value.charAt(value.length() - 1);
                    if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                        value = value.substring(1, value.length() - 1);
                    }
                }
                settings.put(key, value);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the local .env database configuration.", exception);
        }
        return settings;
    }
}
