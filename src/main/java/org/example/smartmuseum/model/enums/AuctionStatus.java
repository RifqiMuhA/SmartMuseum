package org.example.smartmuseum.model.enums;

public enum AuctionStatus {
    UPCOMING("upcoming"),
    ACTIVE("active"),
    ENDED("ended");

    private final String value;

    AuctionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuctionStatus fromString(String value) {
        for (AuctionStatus status : AuctionStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown auction status: " + value);
    }
}