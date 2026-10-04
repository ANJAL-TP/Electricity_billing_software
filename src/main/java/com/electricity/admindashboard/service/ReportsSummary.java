package com.electricity.admindashboard.service;

import java.math.BigDecimal;
import java.util.List;

/** Database-derived totals and breakdowns for administrator reports. */
public record ReportsSummary(
        int totalConsumers,
        int activeConsumers,
        int inactiveConsumers,
        int totalBills,
        BigDecimal totalBilled,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        int totalPayments,
        List<MonthlyRevenue> monthlyRevenue,
        List<PaymentMethodSummary> paymentMethods
) {}
