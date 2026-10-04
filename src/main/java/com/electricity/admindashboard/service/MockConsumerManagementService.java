package com.electricity.admindashboard.service;

import com.electricity.admindashboard.model.ManagedConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** In-memory consumer data retained for standalone UI previews. */
public final class MockConsumerManagementService implements ConsumerManagementService {
    private final List<ManagedConsumer> consumers = new ArrayList<>(List.of(
            new ManagedConsumer("C001", "John Mathew", "Kannur, Kerala", "9876543210", "john@gmail.com", "MTR00123", "LT-1", "ACTIVE"),
            new ManagedConsumer("C002", "Anitha K", "Thalassery, Kerala", "9876501234", "anitha@gmail.com", "MTR00124", "LT-1", "ACTIVE"),
            new ManagedConsumer("C003", "Ramesh P", "Payyanur, Kerala", "9567890123", "ramesh@gmail.com", "MTR00125", "LT-1", "ACTIVE"),
            new ManagedConsumer("C004", "Sujatha T", "Kuthuparamba, Kerala", "9447701122", "sujatha@gmail.com", "MTR00126", "LT-2", "ACTIVE"),
            new ManagedConsumer("C005", "Vishnu V", "Kannur, Kerala", "9846112233", "vishnu@gmail.com", "MTR00127", "HT", "ACTIVE")
    ));

    @Override
    public List<ManagedConsumer> search(String query) {
        String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return consumers.stream().filter(c -> normalized.isEmpty() ||
                (c.consumerId() + " " + c.name() + " " + c.address() + " " + c.phone() + " " +
                        c.email() + " " + c.meterNumber() + " " + c.tariffCode() + " " + c.status())
                        .toLowerCase(Locale.ROOT).contains(normalized)).toList();
    }

    @Override public List<String> getTariffCodes() { return List.of("LT-1", "LT-2", "HT"); }

    @Override
    public void create(ManagedConsumer consumer) {
        if (consumers.stream().anyMatch(c -> c.consumerId().equalsIgnoreCase(consumer.consumerId()) ||
                c.meterNumber().equalsIgnoreCase(consumer.meterNumber()))) {
            throw new IllegalArgumentException("Consumer ID or meter number already exists.");
        }
        consumers.add(consumer);
    }

    @Override
    public void update(String originalConsumerId, ManagedConsumer consumer) {
        int index = indexOf(originalConsumerId);
        if (index < 0) throw new IllegalArgumentException("Consumer record was not found.");
        boolean duplicate = consumers.stream().anyMatch(c -> !c.consumerId().equalsIgnoreCase(originalConsumerId) &&
                (c.consumerId().equalsIgnoreCase(consumer.consumerId()) || c.meterNumber().equalsIgnoreCase(consumer.meterNumber())));
        if (duplicate) throw new IllegalArgumentException("Consumer ID or meter number already exists.");
        consumers.set(index, new ManagedConsumer(consumer.consumerId(), consumer.name(), consumer.address(),
                consumer.phone(), consumer.email(), consumer.meterNumber(), consumer.tariffCode(), consumers.get(index).status()));
    }

    @Override
    public void setActive(String consumerId, boolean active) {
        int index = indexOf(consumerId);
        if (index < 0) throw new IllegalArgumentException("Consumer record was not found.");
        ManagedConsumer c = consumers.get(index);
        consumers.set(index, new ManagedConsumer(c.consumerId(), c.name(), c.address(), c.phone(), c.email(),
                c.meterNumber(), c.tariffCode(), active ? "ACTIVE" : "INACTIVE"));
    }

    private int indexOf(String id) {
        for (int i = 0; i < consumers.size(); i++) {
            if (consumers.get(i).consumerId().equalsIgnoreCase(id)) return i;
        }
        return -1;
    }
}
