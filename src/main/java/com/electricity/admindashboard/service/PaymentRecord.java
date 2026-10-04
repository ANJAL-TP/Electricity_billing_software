package com.electricity.admindashboard.service;

import java.math.BigDecimal;

/** Payment history row, including local consumer requests awaiting confirmation. */
public record PaymentRecord(
        String billNumber,
        String consumerId,
        String consumerName,
        BigDecimal amount,
        String paymentMethod,
        String transactionReference,
        String status,
        String paymentDate
) {}
