package com.electricity.userdashboard.model;

/** Immutable profile details displayed in the consumer dashboard. */
public record ConsumerProfile(
        String username,
        String consumerName,
        String consumerNumber,
        String address,
        String meterNumber,
        String tariffCategory,
        String contactInformation,
        String phone,
        String email
) {
    public ConsumerProfile(String username, String consumerName, String consumerNumber, String address,
                           String meterNumber, String tariffCategory, String contactInformation) {
        this(username, consumerName, consumerNumber, address, meterNumber, tariffCategory,
                contactInformation, firstContact(contactInformation), secondContact(contactInformation));
    }

    private static String firstContact(String contact) {
        if (contact == null || contact.isBlank() || "—".equals(contact)) return "";
        String[] parts = contact.split("\\s+\\|\\s+", 2);
        return parts[0];
    }

    private static String secondContact(String contact) {
        if (contact == null || contact.isBlank() || "—".equals(contact)) return "";
        String[] parts = contact.split("\\s+\\|\\s+", 2);
        return parts.length == 2 ? parts[1] : "";
    }
}
