package com.electricity.billgeneration.model;

/**
 * Immutable record representing consumer and meter summary data for bill generation.
 *
 * @param consumerId            unique consumer account identifier (e.g. EBS-1001)
 * @param fullName              consumer's registered name
 * @param address               installation address
 * @param meterNumber           installed electricity meter serial number
 * @param tariffCategory        tariff category (e.g. Domestic, Commercial, Industrial)
 * @param previousMeterReading  recorded previous cycle meter reading in kWh
 */
public record ConsumerSummary(
        String consumerId,
        String fullName,
        String address,
        String meterNumber,
        String tariffCategory,
        double previousMeterReading
) {
}
