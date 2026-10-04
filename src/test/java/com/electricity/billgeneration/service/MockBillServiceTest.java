package com.electricity.billgeneration.service;

import com.electricity.billgeneration.model.BillCalculationResult;
import com.electricity.billgeneration.model.ConsumerSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockBillServiceTest {

    private MockBillService billService;

    @BeforeEach
    void setUp() {
        billService = new MockBillService();
    }

    @Test
    @DisplayName("Should retrieve registered consumer by ID")
    void testFindConsumerById() {
        Optional<ConsumerSummary> opt = billService.findConsumerById("EBS-1001");
        assertTrue(opt.isPresent());
        ConsumerSummary consumer = opt.get();
        assertEquals("Rajesh Kumar", consumer.fullName());
        assertEquals("MTR-88102", consumer.meterNumber());
        assertEquals(4520.0, consumer.previousMeterReading());
        assertTrue(consumer.tariffCategory().contains("Domestic"));
    }

    @Test
    @DisplayName("Should return empty for unknown consumer ID")
    void testFindUnknownConsumer() {
        Optional<ConsumerSummary> opt = billService.findConsumerById("EBS-9999");
        assertFalse(opt.isPresent());
    }

    @Test
    @DisplayName("Should return all seeded sample consumers")
    void testGetSampleConsumers() {
        List<ConsumerSummary> samples = billService.getActiveConsumers();
        assertNotNull(samples);
        assertTrue(samples.size() >= 4);
    }

    @Test
    @DisplayName("Should accurately calculate domestic tariff charges")
    void testDomesticBillCalculation() {
        ConsumerSummary consumer = billService.findConsumerById("EBS-1001").orElseThrow();
        // 4520 to 4840 = 320 units
        BillCalculationResult result = billService.calculateBill(consumer, 4520.0, 4840.0, "September 2026");

        assertNotNull(result);
        assertEquals(320.0, result.unitsConsumed(), 0.01);
        // Energy charge: 320 * 0.15 = $48.00
        assertEquals(48.00, result.energyCharge(), 0.01);
        // Fixed charge: $10.00
        assertEquals(10.00, result.fixedCharge(), 0.01);
        // Tax: (48 + 10) * 8% = $4.64
        assertEquals(4.64, result.taxAmount(), 0.01);
        // Total: 48 + 10 + 4.64 = $62.64
        assertEquals(62.64, result.totalAmount(), 0.01);
        assertTrue(result.billNumber().startsWith("BILL-"));
    }

    @Test
    @DisplayName("Should accurately calculate commercial tariff charges")
    void testCommercialBillCalculation() {
        ConsumerSummary consumer = billService.findConsumerById("EBS-2001").orElseThrow();
        // 12400 to 13400 = 1000 units
        BillCalculationResult result = billService.calculateBill(consumer, 12400.0, 13400.0, "September 2026");

        assertEquals(1000.0, result.unitsConsumed(), 0.01);
        // Energy charge: 1000 * 0.25 = $250.00
        assertEquals(250.00, result.energyCharge(), 0.01);
        // Fixed charge: $25.00
        assertEquals(25.00, result.fixedCharge(), 0.01);
        // Tax: (250 + 25) * 12% = $33.00
        assertEquals(33.00, result.taxAmount(), 0.01);
        // Total: 250 + 25 + 33 = $308.00
        assertEquals(308.00, result.totalAmount(), 0.01);
    }

    @Test
    @DisplayName("Should generate unique incremental bill numbers")
    void testBillNumberUniqueness() {
        String b1 = billService.generateBillNumber();
        String b2 = billService.generateBillNumber();
        assertNotEquals(b1, b2);
        assertTrue(b1.startsWith("BILL-"));
    }
}
