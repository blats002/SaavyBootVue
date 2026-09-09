package org.saavy.reference;

import lombok.Getter;

@Getter
public enum AttendanceStatus {
    ON_TIME("On Time"),
    LATE("Late"),
    EARLY_DEPARTURE("Early Departure"),
    OVERTIME("Overtime");

    private final String label;

    AttendanceStatus(String label) {
        this.label = label;
    }
}
