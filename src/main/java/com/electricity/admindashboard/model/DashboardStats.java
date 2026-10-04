package com.electricity.admindashboard.model;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Immutable record representing key electricity billing system metrics.
 *
 * @param totalConsumers total registered consumers in the grid
 * @param totalBills     total electricity bills generated
 * @param paidBills      number of settled bills
 * @param unpaidBills    number of outstanding / pending bills
 * @param totalRevenue   total successful payments received
 */
public record DashboardStats(
        int totalConsumers,
        int totalBills,
        int paidBills,
        int unpaidBills,
        double totalRevenue
) {
    public String formattedConsumers() {
        return NumberFormat.getNumberInstance(Locale.US).format(totalConsumers);
    }

    public String formattedBills() {
        return NumberFormat.getNumberInstance(Locale.US).format(totalBills);
    }

    public String formattedPaidBills() {
        return NumberFormat.getNumberInstance(Locale.US).format(paidBills);
    }

    public String formattedUnpaidBills() {
        return NumberFormat.getNumberInstance(Locale.US).format(unpaidBills);
    }

    public String formattedRevenue() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));
        return currencyFormat.format(totalRevenue);
    }

    public double paidPercentage() {
        if (totalBills == 0) return 0.0;
        return ((double) paidBills / totalBills) * 100.0;
    }
}
