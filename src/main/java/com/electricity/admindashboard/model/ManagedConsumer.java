package com.electricity.admindashboard.model;

/** Consumer row used by the administrator's consumer management screen. */
public record ManagedConsumer(
        String consumerId,
        String name,
        String address,
        String phone,
        String email,
        String meterNumber,
        String tariffCode,
        String status
) {}
