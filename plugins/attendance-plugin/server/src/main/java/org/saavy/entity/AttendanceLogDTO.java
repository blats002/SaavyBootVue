package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;
import org.saavy.reference.AttendanceLogType;
import org.saavy.reference.AttendanceStatus;
import org.saavy.reference.VerificationMethod;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(
        title = "Attendance Logs",
        dialogHeader = "Attendance Log Details",
        optionLabel = "employeeName",
        messages = "{\"created\":\"Attendance Log Created\"" +
                ",\"updated\":\"Attendance Log Updated\"" +
                ",\"deleted\":\"Attendance Log Deleted\"" +
                ",\"deletedMany\":\"Attendance Logs Deleted\"" +
                "}",
        masterEndPoint = "attendance-logs"
)
public class AttendanceLogDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Employee ID", type = "text", required = true, sortable = true, order = 2)
    private String employeeId;

    @UiField(label = "Employee Name", type = "text", required = true, sortable = true, order = 3)
    private String employeeName;

    @UiField(label = "Department", type = "text", sortable = true, order = 4)
    private String department;

    @UiField(label = "Timestamp", type = "datetime", required = true, sortable = true, order = 5)
    private LocalDateTime timestamp;

    @UiField(label = "Log Type", type = "enum", sortable = true, order = 6)
    private AttendanceLogType logType;

    @UiField(label = "Method", type = "enum", sortable = true, order = 7)
    private VerificationMethod verificationMethod;

    @UiField(label = "Status", type = "enum", sortable = true, order = 8)
    private AttendanceStatus status;

    @UiField(label = "Kiosk Device", type = "text", order = 9)
    private String kioskDeviceId;

    @UiField(label = "Notes", type = "textarea", order = 10)
    private String notes;
}
