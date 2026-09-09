package org.saavy.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserRoleDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    public Long getId() {
        return role != null ? role.getId() : id;
    }

    public UserRoleDTO(UserDTO userDto, RoleDTO roleDto) {
        this.user = userDto;
        this.role = roleDto;
    }

    @UiField(
            label = "User",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 2,
            optionLabel = "username",
            optionValue = "id",
            optionsEndpoint = "users",
            hidden = true
    )
    @JsonBackReference
    private UserDTO user;

    @UiField(
            label = "Role",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 3,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "roles"
    )
    private RoleDTO role;

    public static String getEmptyRecord() {
        return "{}";
    }
}
