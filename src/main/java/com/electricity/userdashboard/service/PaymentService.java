package com.electricity.userdashboard.service;

/** Records a local pending payment request. It does not transfer or settle money. */
public interface PaymentService {
    String recordPendingPayment(String consumerNumber, String billNumber, String paymentMethod);
}
