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
        title = "Vendor Smart Routing Rules",
        dialogHeader = "Vendor Rule Detail",
        optionLabel = "pattern",
        messages = "{\"created\":\"Vendor Rule Created\",\"updated\":\"Vendor Rule Updated\",\"deleted\":\"Vendor Rule Deleted\",\"deletedMany\":\"Vendor Rules Deleted\"}",
        masterEndPoint = "vendor-rules"
)
public class VendorRuleDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Vendor Pattern (Substring)", type = "text", required = true, order = 2)
    private String pattern;

    @UiField(
            label = "Target Tax Bucket",
            type = "manyToOne",
            required = true,
            order = 3,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "tax-categories"
    )
    private TaxCategoryDTO taxCategory;

    @UiField(label = "Notes", type = "textarea", order = 4)
    private String notes;

    public static String getEmptyRecord() {
        return "{\n" +
                "  \"pattern\": '',\n" +
                "  \"taxCategory\": null,\n" +
                "  \"notes\": ''\n" +
                "}";
    }
}
