package com.electricity.admindashboard.service;

import java.math.BigDecimal;

/** Bill balance details for administrator payment entry. */
public record PaymentBill(
        String billNumber,
        String consumerId,
        String consumerName,
        String billingPeriod,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        String paymentStatus
) {}
