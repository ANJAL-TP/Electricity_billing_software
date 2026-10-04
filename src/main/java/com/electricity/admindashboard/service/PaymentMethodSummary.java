package com.electricity.admindashboard.service;

import java.math.BigDecimal;

public record PaymentMethodSummary(String method, int paymentCount, BigDecimal paidAmount) {}
