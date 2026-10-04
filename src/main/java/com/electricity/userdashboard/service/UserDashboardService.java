package com.electricity.userdashboard.service;

import com.electricity.userdashboard.model.ConsumerBill;
import com.electricity.userdashboard.model.ConsumerProfile;

import java.util.List;

/** Data boundary for profile and billing information in the consumer dashboard. */
public interface UserDashboardService {
    ConsumerProfile getConsumerProfile(String username);

    ConsumerBill getCurrentBill(String consumerNumber);

    List<ConsumerBill> getBillHistory(String consumerNumber);

    /** Update only contact fields for the consumer linked to this signed-in user. */
    default boolean updateContactDetails(String username, String address, String phone, String email) { return false; }
}
