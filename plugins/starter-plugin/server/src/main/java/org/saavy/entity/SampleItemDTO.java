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
        title = "Sample Items",
        dialogHeader = "Sample Item Details",
        optionLabel = "name",
        messages = "{\"created\":\"Sample Item Created\"" +
                ",\"updated\":\"Sample Item Updated\"" +
                ",\"deleted\":\"Sample Item Deleted\"" +
                ",\"deletedMany\":\"Sample Items Deleted\"" +
                "}",
        masterEndPoint = "sample-items"
)
public class SampleItemDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Name", type = "text", required = true, sortable = true, order = 2)
    private String name;

    @UiField(label = "Description", type = "text", sortable = true, order = 3)
    private String description;

    @UiField(label = "Price", type = "number", required = true, sortable = true, order = 4)
    private double price;

    @UiField(label = "Active", type = "checkbox", sortable = true, order = 5)
    private Boolean active = true;
}
