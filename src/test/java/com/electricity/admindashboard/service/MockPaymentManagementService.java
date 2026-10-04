package com.electricity.admindashboard.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Empty payment service for UI previews and isolated Swing construction. */
public final class MockPaymentManagementService implements PaymentManagementService {
    @Override public Optional<PaymentBill> findBill(String billNumber) { return Optional.empty(); }
    @Override public List<PaymentRecord> getRecentPayments() { return List.of(); }
    @Override public PaymentBill recordPayment(String billNumber, BigDecimal amount, String paymentMethod, String transactionReference) {
        throw new UnsupportedOperationException("Payment recording requires the MySQL service.");
    }
}
