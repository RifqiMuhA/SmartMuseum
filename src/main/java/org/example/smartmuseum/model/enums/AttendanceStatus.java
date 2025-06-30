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

    public static AttendanceStatus fromValue(String value) {
        if (value == null) return ABSENT;

        for (AttendanceStatus status : AttendanceStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return ABSENT; // Default fallback
    }
}
