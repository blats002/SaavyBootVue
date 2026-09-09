package org.saavy.reference;

import lombok.Getter;

@Getter
public enum AttendanceLogType {
    CLOCK_IN("Clock In"),
    CLOCK_OUT("Clock Out"),
    BREAK_OUT("Break Out"),
    BREAK_IN("Break In");

    private final String label;

    AttendanceLogType(String label) {
        this.label = label;
    }
}
