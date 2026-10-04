package com.electricity.admindashboard.service;

import java.math.BigDecimal;
import java.util.List;

/** Zero-valued report source used only by UI preview constructors. */
public final class MockReportsService implements ReportsService {
    @Override public ReportsSummary loadReports() {
        return new ReportsSummary(0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                0, List.of(), List.of());
    }
}
