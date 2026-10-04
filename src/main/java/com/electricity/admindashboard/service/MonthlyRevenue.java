package com.electricity.admindashboard.service;

import java.math.BigDecimal;

public record MonthlyRevenue(String billingPeriod, int billCount, BigDecimal billed, BigDecimal paid, BigDecimal outstanding) {}
