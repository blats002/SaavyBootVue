package org.saavy.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO implements Serializable {
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(
            label = "Product",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 2,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "product"
    )
    @JsonBackReference
    private ProductDTO product;

    @UiField(
            label = "Supplier",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 3,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "supplier"
    )
    private SupplierDTO supplier;

    @UiField(label = "Quantity", type = "number", order = 4)
    private int quantity;

    @JsonProperty("displayValue")
    private String displayString;

    public static String getEmptyRecord() {
        return "{}";
    }
}
