package org.saavy.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Username", type = "text", required = true, sortable = true, order = 2)
    private String username;

    @UiField(label = "Full Name", type = "text", required = true, sortable = true, order = 3)
    private String fullName;

    @UiField(label = "Email", type = "text", required = true, sortable = true, order = 4)
    private String email;

    @UiField(label = "Password", type = "text", order = 5)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @UiField(
            label = "Primary Role",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 6,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "roles"
    )
    private RoleDTO role;

    @UiField(label = "Enabled", type = "checkbox", sortable = true, order = 7)
    private Boolean enabled = true;

    private Set<RoleDTO> roles = new HashSet<>();

    @JsonProperty("displayValue")
    private String displayString;

    public static String getEmptyRecord() {
        return "{}";
    }
}
