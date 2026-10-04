package com.electricity.billgeneration.service;

import com.electricity.billgeneration.model.BillCalculationResult;
import com.electricity.billgeneration.model.ConsumerSummary;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock implementation of {@link BillService} providing realistic sample consumers
 * and separated sample calculation logic without requiring MySQL or external backends.
 */
public class MockBillService implements BillService {

    private final Map<String, ConsumerSummary> mockConsumerDb = new ConcurrentHashMap<>();
    private final AtomicInteger billSequence = new AtomicInteger(1001);

    public MockBillService() {
        seedConsumers();
    }

    private void seedConsumers() {
        mockConsumerDb.put("EBS-1001", new ConsumerSummary(
                "EBS-1001",
                "Rajesh Kumar",
                "142 Green Avenue, Phase 2, North District",
                "MTR-88102",
                "Domestic (LT-1)",
                4520.0
        ));

        mockConsumerDb.put("EBS-1002", new ConsumerSummary(
                "EBS-1002",
                "Anita Sharma",
                "Flat 4B, Skyline Heights, Civil Lines",
                "MTR-99201",
                "Domestic (LT-1)",
                3100.0
        ));

        mockConsumerDb.put("EBS-2001", new ConsumerSummary(
                "EBS-2001",
                "Metro Mart & Retail",
                "Plot 28, Commercial Zone, Sector 18",
                "MTR-77150",
                "Commercial (LT-2)",
                12400.0
        ));

        mockConsumerDb.put("EBS-3001", new ConsumerSummary(
                "EBS-3001",
                "Apex Engineering Ltd",
                "Phase 1 Industrial Corridor, Plot 502",
                "MTR-55420",
                "Industrial (HT)",
                58900.0
        ));
    }

    @Override
    public Optional<ConsumerSummary> findConsumerById(String consumerId) {
        if (consumerId == null || consumerId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mockConsumerDb.get(consumerId.trim().toUpperCase()));
    }

    @Override
    public List<ConsumerSummary> getSampleConsumers() {
        return new ArrayList<>(mockConsumerDb.values());
    }

    @Override
    public BillCalculationResult calculateBill(
            ConsumerSummary consumer,
            double previousReading,
            double currentReading,
            String billingPeriod
    ) {
        double units = Math.max(0.0, currentReading - previousReading);

        // Tariff parameters depending on category
        double ratePerUnit;
        double fixedCharge;
        double taxRate;

        String category = consumer != null && consumer.tariffCategory() != null
                ? consumer.tariffCategory()
                : "Domestic (LT-1)";

        if (category.toLowerCase().contains("commercial")) {
            ratePerUnit = 0.25;
            fixedCharge = 25.00;
            taxRate = 0.12; // 12%
        } else if (category.toLowerCase().contains("industrial")) {
            ratePerUnit = 0.35;
            fixedCharge = 50.00;
            taxRate = 0.15; // 15%
        } else {
            // Default Domestic
            ratePerUnit = 0.15;
            fixedCharge = 10.00;
            taxRate = 0.08; // 8%
        }

        double energyCharge = Math.round(units * ratePerUnit * 100.0) / 100.0;
        double subtotal = energyCharge + fixedCharge;
        double taxAmount = Math.round(subtotal * taxRate * 100.0) / 100.0;
        double totalAmount = Math.round((subtotal + taxAmount) * 100.0) / 100.0;

        String billNumber = generateBillNumber();

        return new BillCalculationResult(
                billNumber,
                billingPeriod,
                consumer != null ? consumer.consumerId() : "N/A",
                consumer != null ? consumer.fullName() : "N/A",
                consumer != null ? consumer.address() : "N/A",
                consumer != null ? consumer.meterNumber() : "N/A",
                category,
                previousReading,
                currentReading,
                units,
                energyCharge,
                fixedCharge,
                taxAmount,
                totalAmount
        );
    }

    @Override
    public BillCalculationResult generateBill(BillCalculationResult calculation) {
        return calculation;
    }

    @Override
    public String generateBillNumber() {
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "BILL-" + yearMonth + "-" + billSequence.getAndIncrement();
    }
}
