package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.reference.AttendanceLogType;
import org.saavy.reference.AttendanceStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceScanResponse {
    private boolean success;
    private String message;
    private String employeeId;
    private String employeeName;
    private String department;
    private String jobTitle;
    private String avatar;
    private AttendanceLogType logType;
    private AttendanceStatus status;
    private LocalDateTime timestamp;
}