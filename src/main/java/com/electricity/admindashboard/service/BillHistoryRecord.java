package com.electricity.admindashboard.service;

import com.electricity.billgeneration.model.BillCalculationResult;

/** Full bill details shown in administrator bill history and invoice preview. */
public record BillHistoryRecord(
        BillCalculationResult bill,
        String consumerName,
        String generatedAt,
        String dueDate,
        String paymentStatus
) {}
