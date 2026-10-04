package com.electricity.admindashboard.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Administrator queries and settlement of received payments. */
public interface PaymentManagementService {
    Optional<PaymentBill> findBill(String billNumber);
    List<PaymentRecord> getRecentPayments();
    PaymentBill recordPayment(String billNumber, BigDecimal amount, String paymentMethod, String transactionReference);
}
