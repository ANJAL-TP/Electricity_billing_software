/*package com.electricity.billgeneration.model;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Immutable record representing the computed line items of an electricity bill.
 */
/*public record BillCalculationResult(
        String billNumber,
        String billingPeriod,
        String consumerId,
        String consumerName,
        String address,
        String meterNumber,
        String tariffCategory,
        double previousReading,
        double currentReading,
        double unitsConsumed,
        double energyCharge,
        double fixedCharge,
        double taxAmount,
        double totalAmount
) {
    public String formattedEnergyCharge() {
        return formatCurrency(energyCharge);
    }

    public String formattedFixedCharge() {
        return formatCurrency(fixedCharge);
    }

    public String formattedTaxAmount() {
        return formatCurrency(taxAmount);
    }

    public String formattedTotalAmount() {
        return formatCurrency(totalAmount);
    }

    public String formattedUnits() {
        return String.format(Locale.US, "%.1f kWh", unitsConsumed);
    }

    private static String formatCurrency(double value) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormat.format(value);
    }
}
*/
package com.electricity.billgeneration.model;

import java.util.Locale;

/**
 * Immutable record representing the computed line items of an electricity bill.
 */
public record BillCalculationResult(
        String billNumber,
        String billingPeriod,
        String consumerId,
        String consumerName,
        String address,
        String meterNumber,
        String tariffCategory,
        double previousReading,
        double currentReading,
        double unitsConsumed,
        double energyCharge,
        double fixedCharge,
        double taxAmount,
        double totalAmount
) {
    public String formattedEnergyCharge() {
        return formatCurrency(energyCharge);
    }

    public String formattedFixedCharge() {
        return formatCurrency(fixedCharge);
    }

    public String formattedTaxAmount() {
        return formatCurrency(taxAmount);
    }

    public String formattedTotalAmount() {
        return formatCurrency(totalAmount);
    }

    public String formattedUnits() {
        return String.format(Locale.US, "%.0f kWh", unitsConsumed);
    }


    private static String formatCurrency(double value) {
        return String.format("Rs.%.2f", value);
}
}
