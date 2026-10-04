package com.electricity.billgeneration.service;

import com.electricity.billgeneration.model.BillCalculationResult;
import com.electricity.billgeneration.model.ConsumerSummary;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for Bill Generation operations.
 * Decouples the Swing UI from the database queries and tariff calculation engine.
 */
public interface BillService {

    /**
     * Finds a consumer by their unique Consumer ID.
     */
    Optional<ConsumerSummary> findConsumerById(String consumerId);

    /** Returns active registered consumers for UI selection. */
    List<ConsumerSummary> getSampleConsumers();

    /**
     * Calculates the bill line items based on readings and tariff category.
     */
    BillCalculationResult calculateBill(
            ConsumerSummary consumer,
            double previousReading,
            double currentReading,
            String billingPeriod
    );

    /** Persists a calculated bill and its meter reading, returning the saved invoice number. */
    BillCalculationResult generateBill(BillCalculationResult calculation);

    /**
     * Generates a unique sequential bill invoice number.
     */
    String generateBillNumber();
}
