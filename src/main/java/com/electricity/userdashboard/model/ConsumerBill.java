package com.electricity.userdashboard.model;

import java.math.BigDecimal;

/** Immutable bill data displayed by the consumer dashboard. */
public record ConsumerBill(
        String billNumber,
        String billingPeriod,
        BigDecimal previousMeterReading,
        BigDecimal currentMeterReading,
        BigDecimal unitsConsumed,
        BigDecimal energyCharge,
        BigDecimal fixedCharge,
        BigDecimal tax,
        BigDecimal totalAmount,
        BigDecimal outstandingAmount,
        String paymentStatus,
        String dueDate
) {
    public ConsumerBill(
            String billNumber, String billingPeriod, BigDecimal previousMeterReading,
            BigDecimal currentMeterReading, BigDecimal unitsConsumed, BigDecimal energyCharge,
            BigDecimal fixedCharge, BigDecimal tax, BigDecimal totalAmount,
            String paymentStatus, String dueDate
    ) {
        this(billNumber, billingPeriod, previousMeterReading, currentMeterReading, unitsConsumed,
                energyCharge, fixedCharge, tax, totalAmount, totalAmount, paymentStatus, dueDate);
    }

    public static ConsumerBill empty() {
        BigDecimal zero = BigDecimal.ZERO;
        return new ConsumerBill("—", "—", zero, zero, zero, zero, zero, zero, zero, zero, "No bill", "—");
    }
}
