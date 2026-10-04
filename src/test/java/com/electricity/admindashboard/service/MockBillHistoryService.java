package com.electricity.admindashboard.service;

import java.util.List;

/** Empty bill-history service for UI previews and isolated Swing construction. */
public final class MockBillHistoryService implements BillHistoryService {
    @Override public List<String> getBillingPeriods() { return List.of(); }
    @Override public List<BillHistoryRecord> search(String query, String billingPeriod, String paymentStatus) { return List.of(); }
}
