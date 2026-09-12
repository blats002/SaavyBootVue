package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(
        title = "Tax Categories & Buckets",
        dialogHeader = "Tax Category Detail",
        optionLabel = "name",
        messages = "{\"created\":\"Tax Category Created\",\"updated\":\"Tax Category Updated\",\"deleted\":\"Tax Category Deleted\",\"deletedMany\":\"Tax Categories Deleted\"}",
        masterEndPoint = "tax-categories"
)
public class TaxCategoryDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Category Code", type = "text", required = true, order = 2)
    private String code;

    @UiField(label = "Category Name", type = "text", required = true, order = 3)
    private String name;

    @UiField(label = "P&L Mapping", type = "enum", required = true, order = 4, enumOptions = "[{\"label\":\"Revenue\",\"value\":\"REVENUE\"},{\"label\":\"Cost of Goods Sold (COGS)\",\"value\":\"COGS\"},{\"label\":\"Operating Expense (OPEX)\",\"value\":\"OPEX\"},{\"label\":\"Other Income\",\"value\":\"OTHER_INCOME\"},{\"label\":\"Other Expense\",\"value\":\"OTHER_EXPENSE\"}]")
    private String pnlCategory;

    @UiField(label = "BIR Schedule Line", type = "text", order = 5)
    private String birScheduleLine;

    @UiField(label = "Tax Deductible", type = "boolean", order = 6)
    private Boolean isDeductible = true;

    @UiField(label = "Statutory Cap %", type = "number", order = 7)
    private BigDecimal statutoryCapPercent;

    @UiField(label = "Description", type = "textarea", order = 8)
    private String description;

    public static String getEmptyRecord() {
        return "{\n" +
                "  \"code\": '',\n" +
                "  \"name\": '',\n" +
                "  \"pnlCategory\": 'OPEX',\n" +
                "  \"birScheduleLine\": '',\n" +
                "  \"isDeductible\": true,\n" +
                "  \"statutoryCapPercent\": null,\n" +
                "  \"description\": ''\n" +
                "}";
    }
}
