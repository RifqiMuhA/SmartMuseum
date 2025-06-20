package org.example.smartmuseum.model.enums;

public enum AttendanceStatus {
    PRESENT("present"),
    ABSENT("absent"),
    LATE("late");

    private final String value;

    AttendanceStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AttendanceStatus fromString(String value) {
        for (AttendanceStatus status : AttendanceStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown attendance status: " + value);
    }
}