package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(
        title = "Employee Badges",
        dialogHeader = "Employee Badge Details",
        optionLabel = "employeeName",
        messages = "{\"created\":\"Badge Created\"" +
                ",\"updated\":\"Badge Updated\"" +
                ",\"deleted\":\"Badge Deleted\"" +
                ",\"deletedMany\":\"Badges Deleted\"" +
                "}",
        masterEndPoint = "employee-badges"
)
public class EmployeeBadgeDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Employee ID", type = "text", required = true, sortable = true, order = 2)
    private String employeeId;

    @UiField(label = "Employee Name", type = "text", required = true, sortable = true, order = 3)
    private String employeeName;

    @UiField(label = "Department", type = "text", sortable = true, order = 4)
    private String department;

    @UiField(label = "Job Title", type = "text", sortable = true, order = 5)
    private String jobTitle;

    @UiField(label = "QR Token", type = "qrcode", required = true, sortable = true, order = 6)
    private String qrToken;

    @UiField(label = "PIN Code", type = "text", order = 7)
    private String pinCode;

    @UiField(label = "Active", type = "checkbox", sortable = true, order = 8)
    private Boolean isActive = true;

    @UiField(
            label = "Avatar Image",
            type = "BaseFile",
            order = 9
    )
    private AvatarFileDTO avatarFile;
}