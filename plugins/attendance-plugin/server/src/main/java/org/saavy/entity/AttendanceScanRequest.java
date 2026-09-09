package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.reference.AttendanceLogType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceScanRequest {
    private String qrToken;
    private String pinCode;
    private String kioskDeviceId;
    private AttendanceLogType preferredAction; // Optional override (e.g. user specifically clicks Break In)
}
