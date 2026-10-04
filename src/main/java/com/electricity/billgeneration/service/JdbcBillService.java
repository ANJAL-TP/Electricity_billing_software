package com.electricity.billgeneration.service;

import com.electricity.billgeneration.model.BillCalculationResult;
import com.electricity.billgeneration.model.ConsumerSummary;
import com.electricity.config.DatabaseAccessException;
import com.electricity.config.DatabaseConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Reads tariff data and creates meter-reading and bill rows atomically in MySQL. */
public final class JdbcBillService implements BillService {
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private final String actorUsername;

    public JdbcBillService(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    @Override
    public Optional<ConsumerSummary> findConsumerById(String consumerId) {
        if (consumerId == null || consumerId.isBlank()) return Optional.empty();
        String sql = consumerSelect() + " WHERE c.consumer_id = ? AND c.status = 'ACTIVE'";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, consumerId.trim());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapConsumer(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to find the consumer in MySQL.", exception);
        }
    }

    @Override
    public List<ConsumerSummary> getSampleConsumers() {
        String sql = consumerSelect() + " WHERE c.status = 'ACTIVE' ORDER BY c.consumer_id";
        List<ConsumerSummary> consumers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) consumers.add(mapConsumer(result));
            return List.copyOf(consumers);
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to load active consumers from MySQL.", exception);
        }
    }

    @Override
    public BillCalculationResult calculateBill(
            ConsumerSummary consumer,
            double previousReading,
            double currentReading,
            String billingPeriod
    ) {
        if (consumer == null) throw new IllegalArgumentException("Search for an active consumer first.");
        BigDecimal previous = reading(previousReading, "Previous reading");
        BigDecimal current = reading(currentReading, "Current reading");
        if (current.compareTo(previous) < 0) {
            throw new IllegalArgumentException("Current reading cannot be less than previous reading.");
        }

        YearMonth period = parsePeriod(billingPeriod);
        try (Connection connection = DatabaseConnection.getConnection()) {
            ConsumerSummary savedConsumer = findConsumer(connection, consumer.consumerId())
                    .orElseThrow(() -> new IllegalArgumentException("This consumer is no longer active."));
            BigDecimal savedPrevious = reading(savedConsumer.previousMeterReading(), "Saved previous reading");
            if (savedPrevious.compareTo(previous) != 0) {
                throw new IllegalArgumentException("Previous reading changed in the database. Search the consumer again.");
            }
            String lastPeriodText = latestReadingPeriod(connection, consumer.consumerId());
            if (lastPeriodText != null && !period.isAfter(YearMonth.parse(lastPeriodText))) {
                throw new IllegalArgumentException("Choose a billing period later than the latest recorded meter reading (" + lastPeriodText + ").");
            }
            if (period.isAfter(YearMonth.now())) {
                throw new IllegalArgumentException("A bill cannot be generated for a future billing period.");
            }

            Tariff tariff = loadTariff(connection, consumer.consumerId());
            BigDecimal units = current.subtract(previous).setScale(3, RoundingMode.HALF_UP);
            BigDecimal energy = calculateEnergyCharge(connection, tariff.id(), units).setScale(2, RoundingMode.HALF_UP);
            BigDecimal fixed = tariff.fixedCharge().setScale(2, RoundingMode.HALF_UP);
            BigDecimal tax = energy.multiply(tariff.taxPercentage()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            BigDecimal total = energy.add(fixed).add(tax).setScale(2, RoundingMode.HALF_UP);

            return new BillCalculationResult(
                    "Generated when saved",
                    period.toString(),
                    savedConsumer.consumerId(),
                    savedConsumer.fullName(),
                    savedConsumer.address(),
                    savedConsumer.meterNumber(),
                    tariff.name() + " (" + tariff.code() + ")",
                    previous.doubleValue(),
                    current.doubleValue(),
                    units.doubleValue(),
                    energy.doubleValue(),
                    fixed.doubleValue(),
                    tax.doubleValue(),
                    total.doubleValue()
            );
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to calculate the bill from the active MySQL tariff.", exception);
        }
    }

    @Override
    public BillCalculationResult generateBill(BillCalculationResult calculation) {
        if (calculation == null) throw new IllegalArgumentException("Calculate a bill before saving it.");
        YearMonth period = parsePeriod(calculation.billingPeriod());
        BigDecimal previous = reading(calculation.previousReading(), "Previous reading");
        BigDecimal current = reading(calculation.currentReading(), "Current reading");
        BigDecimal units = reading(calculation.unitsConsumed(), "Units consumed");
        if (current.compareTo(previous) < 0 || current.subtract(previous).compareTo(units) != 0) {
            throw new IllegalArgumentException("Meter readings are inconsistent. Calculate the bill again.");
        }

        String billNumber = generateBillNumber(period);
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ConsumerSummary currentConsumer = findConsumer(connection, calculation.consumerId())
                        .orElseThrow(() -> new IllegalArgumentException("This consumer is no longer active."));
                BigDecimal latestReading = reading(currentConsumer.previousMeterReading(), "Saved previous reading");
                if (latestReading.compareTo(previous) != 0) {
                    throw new IllegalArgumentException("A newer meter reading was saved. Search the consumer and recalculate.");
                }
                Tariff activeTariff = loadTariff(connection, calculation.consumerId());
                if (!activeTariff.code().equals(tariffCode(calculation.tariffCategory()))) {
                    throw new IllegalArgumentException("The assigned tariff changed. Search the consumer and recalculate.");
                }
                BigDecimal energy = calculateEnergyCharge(connection, activeTariff.id(), units).setScale(2, RoundingMode.HALF_UP);
                BigDecimal fixed = activeTariff.fixedCharge().setScale(2, RoundingMode.HALF_UP);
                BigDecimal tax = energy.multiply(activeTariff.taxPercentage()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
                BigDecimal total = energy.add(fixed).add(tax).setScale(2, RoundingMode.HALF_UP);
                if (money(calculation.energyCharge()).compareTo(energy) != 0
                        || money(calculation.fixedCharge()).compareTo(fixed) != 0
                        || money(calculation.taxAmount()).compareTo(tax) != 0
                        || money(calculation.totalAmount()).compareTo(total) != 0) {
                    throw new IllegalArgumentException("Tariff rates changed. Calculate the bill again before saving.");
                }

                long meterReadingId;
                String readingSql = """
                        INSERT INTO meter_readings
                            (consumer_id, previous_reading, current_reading, units_consumed, reading_date, billing_period)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement statement = connection.prepareStatement(readingSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, calculation.consumerId());
                    statement.setBigDecimal(2, previous);
                    statement.setBigDecimal(3, current);
                    statement.setBigDecimal(4, units);
                    statement.setDate(5, Date.valueOf(LocalDate.now()));
                    statement.setString(6, period.toString());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("MySQL did not return the meter reading ID.");
                        meterReadingId = keys.getLong(1);
                    }
                }

                String billSql = """
                        INSERT INTO bills
                            (bill_number, consumer_id, meter_reading_id, billing_period, previous_reading,
                             current_reading, units_consumed, tariff_category, energy_charge, fixed_charge,
                             tax_charge, other_charge, total_amount, payment_status, due_date)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0.00, ?, 'UNPAID', ?)
                        """;
                try (PreparedStatement statement = connection.prepareStatement(billSql)) {
                    statement.setString(1, billNumber);
                    statement.setString(2, calculation.consumerId());
                    statement.setLong(3, meterReadingId);
                    statement.setString(4, period.toString());
                    statement.setBigDecimal(5, previous);
                    statement.setBigDecimal(6, current);
                    statement.setBigDecimal(7, units);
                    statement.setString(8, activeTariff.code());
                    statement.setBigDecimal(9, energy);
                    statement.setBigDecimal(10, fixed);
                    statement.setBigDecimal(11, tax);
                    statement.setBigDecimal(12, total);
                    statement.setDate(13, Date.valueOf(LocalDate.now().plusDays(30)));
                    statement.executeUpdate();
                }
                addActivity(connection, "METER_READING_ADDED", "Meter reading saved for " + calculation.consumerId()
                        + " for " + period);
                addActivity(connection, "BILL_GENERATED", "Bill " + billNumber + " generated for " + calculation.consumerId());
                connection.commit();
                return new BillCalculationResult(
                        billNumber, calculation.billingPeriod(), calculation.consumerId(), currentConsumer.fullName(),
                        currentConsumer.address(), currentConsumer.meterNumber(),
                        activeTariff.name() + " (" + activeTariff.code() + ")",
                        calculation.previousReading(), calculation.currentReading(), calculation.unitsConsumed(),
                        energy.doubleValue(), fixed.doubleValue(), tax.doubleValue(), total.doubleValue()
                );
            } catch (IllegalArgumentException exception) {
                connection.rollback();
                throw exception;
            } catch (SQLException exception) {
                connection.rollback();
                if ("23000".equals(exception.getSQLState())) {
                    throw new IllegalArgumentException("A meter reading or bill already exists for this consumer and billing period.", exception);
                }
                throw exception;
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (SQLException exception) {
            throw new DatabaseAccessException("Unable to save the bill to MySQL.", exception);
        }
    }

    @Override
    public String generateBillNumber() {
        return generateBillNumber(YearMonth.now());
    }

    private Optional<ConsumerSummary> findConsumer(Connection connection, String consumerId) throws SQLException {
        String sql = consumerSelect() + " WHERE c.consumer_id = ? AND c.status = 'ACTIVE'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, consumerId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapConsumer(result)) : Optional.empty();
            }
        }
    }

    private static String consumerSelect() {
        return """
                SELECT c.consumer_id, c.name, COALESCE(c.address, '') AS address, c.meter_number,
                       t.tariff_name, c.tariff_category,
                       COALESCE((SELECT mr.current_reading FROM meter_readings mr
                                 WHERE mr.consumer_id = c.consumer_id
                                 ORDER BY mr.billing_period DESC, mr.id DESC LIMIT 1), 0) AS previous_reading
                FROM consumers c JOIN tariffs t ON t.tariff_code = c.tariff_category
                """;
    }

    private static ConsumerSummary mapConsumer(ResultSet result) throws SQLException {
        return new ConsumerSummary(
                result.getString("consumer_id"), result.getString("name"), result.getString("address"),
                result.getString("meter_number"), result.getString("tariff_name") + " (" + result.getString("tariff_category") + ")",
                result.getBigDecimal("previous_reading").doubleValue()
        );
    }

    private static String latestReadingPeriod(Connection connection, String consumerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT MAX(billing_period) FROM meter_readings WHERE consumer_id = ?")) {
            statement.setString(1, consumerId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        }
    }

    private static Tariff loadTariff(Connection connection, String consumerId) throws SQLException {
        String sql = """
                SELECT t.id, t.tariff_code, t.tariff_name, t.fixed_charge, t.tax_percentage
                FROM consumers c JOIN tariffs t ON t.tariff_code = c.tariff_category
                WHERE c.consumer_id = ? AND c.status = 'ACTIVE' AND t.active = TRUE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, consumerId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("No active tariff is assigned to this consumer.");
                return new Tariff(result.getLong("id"), result.getString("tariff_code"),
                        result.getString("tariff_name"), result.getBigDecimal("fixed_charge"),
                        result.getBigDecimal("tax_percentage"));
            }
        }
    }

    private static BigDecimal calculateEnergyCharge(Connection connection, long tariffId, BigDecimal units) throws SQLException {
        String sql = "SELECT min_units, max_units, rate_per_unit FROM tariff_slabs WHERE tariff_id = ? ORDER BY min_units";
        BigDecimal charge = BigDecimal.ZERO;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, tariffId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    BigDecimal lower = result.getBigDecimal("min_units");
                    BigDecimal upper = result.getBigDecimal("max_units");
                    BigDecimal end = upper == null || units.compareTo(upper) < 0 ? units : upper;
                    if (end.compareTo(lower) > 0) {
                        charge = charge.add(end.subtract(lower).multiply(result.getBigDecimal("rate_per_unit")));
                    }
                }
            }
        }
        return charge;
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

    private static BigDecimal reading(double value, String label) {
        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException(label + " must be a non-negative number.");
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(double value) {
        if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("Bill amounts must be non-negative.");
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static YearMonth parsePeriod(String period) {
        try {
            return YearMonth.parse(period);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Billing period must use YYYY-MM format.", exception);
        }
    }

    private static String tariffCode(String tariffDisplay) {
        if (tariffDisplay == null) throw new IllegalArgumentException("Tariff is required.");
        int open = tariffDisplay.lastIndexOf('(');
        int close = tariffDisplay.lastIndexOf(')');
        if (open < 0 || close <= open) throw new IllegalArgumentException("Tariff code could not be determined.");
        return tariffDisplay.substring(open + 1, close).trim();
    }

    private static String generateBillNumber(YearMonth period) {
        String month = period.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "EB-" + month + "-" + suffix;
    }

    private record Tariff(long id, String code, String name, BigDecimal fixedCharge, BigDecimal taxPercentage) {}
}
