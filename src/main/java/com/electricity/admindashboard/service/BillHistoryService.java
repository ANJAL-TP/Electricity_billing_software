package com.electricity.admindashboard.service;

import java.util.List;

/** Query operations for the administrator's bill history screen. */
public interface BillHistoryService {
    List<String> getBillingPeriods();
    List<BillHistoryRecord> search(String query, String billingPeriod, String paymentStatus);
}
