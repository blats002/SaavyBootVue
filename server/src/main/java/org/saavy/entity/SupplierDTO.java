package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO implements Serializable {
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;
    @UiField(label = "Name", type = "text", order = 2)
    private String name;
    @UiField(label = "Contact Email", type = "text", order = 3)
    private String contactEmail;

    public static String getEmptyRecord() {
        return "{}";
    }
}
