package com.electricity.userdashboard.service;

import java.util.UUID;

/** Local-only payment request stand-in for standalone UI previews. */
public final class MockPaymentService implements PaymentService {
    @Override
    public String recordPendingPayment(String consumerNumber, String billNumber, String paymentMethod) {
        return "LOCAL-PENDING-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
