package org.saavy.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiDetail;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(title = "Users",
        dialogHeader = "Assigned Roles",
        optionLabel = "displayValue",
        messages = "{\"created\":\"User Created\"" +
                ",\"updated\":\"User Updated\"" +
                ",\"deleted\":\"User Deleted\"" +
                ",\"deletedMany\":\"User Deleted\"" +
                "}",
        masterEndPoint = "users"
)
public class UserDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Username", type = "text", required = true, sortable = true, order = 2)
    private String username;

    @UiField(label = "Full Name", type = "text", required = true, sortable = true, order = 3)
    private String fullName;

    @UiField(label = "Email", type = "text", required = true, sortable = true, order = 4)
    private String email;

    @UiField(label = "Password", type = "password", order = 5)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @UiField(label = "Enabled", type = "checkbox", sortable = true, order = 7)
    private Boolean enabled = true;

    @UiDetail(
            key = "roles",
            title = "Roles",
            parentField = "user",
            messages = "{\"created\":\"Role Assigned\"" +
                    ",\"updated\":\"Role Updated\"" +
                    ",\"deleted\":\"Role Removed\"" +
                    ",\"deletedMany\":\"Roles Removed\"" +
                    "}",
            detailEndpoint = "userrole",
            deleteWithPayload = true
    )
    private java.util.List<UserRoleDTO> userRoles = new ArrayList<>();

    private Set<RoleDTO> roles = new HashSet<>();

    @JsonProperty("displayValue")
    private String displayString;

    public static String getEmptyRecord() {
        return "{}";
    }
}
