package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Role Name", type = "text", required = true, order = 2)
    private String name;

    @UiField(label = "Description", type = "text", order = 3)
    private String description;

    public static String getEmptyRecord() {
        return "{}";
    }
}
