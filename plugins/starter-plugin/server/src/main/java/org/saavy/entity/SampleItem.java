package org.saavy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;

@Entity
@Table(name = "sample_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(title = "Sample Items",
        dialogHeader = "Sample Item Details",
        optionLabel = "name",
        messages = "{\"created\":\"Sample Item Created\"" +
                ",\"updated\":\"Sample Item Updated\"" +
                ",\"deleted\":\"Sample Item Deleted\"" +
                ",\"deletedMany\":\"Sample Items Deleted\"" +
                "}",
        masterEndPoint = "sample-items"
)
public class SampleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @Column(nullable = false)
    @UiField(label = "Name", type = "text", required = true, sortable = true, order = 2)
    private String name;

    @Column
    @UiField(label = "Description", type = "text", sortable = true, order = 3)
    private String description;

    @Column(nullable = false)
    @UiField(label = "Price", type = "number", required = true, sortable = true, order = 4)
    private double price;

    @Column
    @UiField(label = "Active", type = "checkbox", sortable = true, order = 5)
    private Boolean active = true;
}
