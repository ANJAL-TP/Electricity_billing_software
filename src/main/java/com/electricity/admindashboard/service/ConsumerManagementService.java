package com.electricity.admindashboard.service;

import com.electricity.admindashboard.model.ManagedConsumer;

import java.util.List;

/** Persistence boundary for administrator consumer search and maintenance. */
public interface ConsumerManagementService {
    List<ManagedConsumer> search(String query);
    List<String> getTariffCodes();
    void create(ManagedConsumer consumer);
    void update(String originalConsumerId, ManagedConsumer consumer);
    void setActive(String consumerId, boolean active);
}
