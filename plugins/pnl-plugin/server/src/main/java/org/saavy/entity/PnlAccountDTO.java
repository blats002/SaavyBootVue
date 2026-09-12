package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;
import org.saavy.reference.PnlAccountCategory;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(
        title = "Chart of Accounts",
        dialogHeader = "Account Details",
        optionLabel = "name",
        messages = "{\"created\":\"Account Created\"" +
                ",\"updated\":\"Account Updated\"" +
                ",\"deleted\":\"Account Deleted\"" +
                ",\"deletedMany\":\"Accounts Deleted\"" +
                "}",
        masterEndPoint = "pnl-accounts"
)
public class PnlAccountDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Account Code", type = "text", required = true, sortable = true, order = 2)
    private String code;

    @UiField(label = "Account Name", type = "text", required = true, sortable = true, order = 3)
    private String name;

    @UiField(label = "Category", type = "enum", required = true, sortable = true, order = 4)
    private PnlAccountCategory category;

    @UiField(label = "Subcategory / Dept", type = "text", sortable = true, order = 5)
    private String subcategory;

    @UiField(label = "Sort Order", type = "number", sortable = true, order = 6)
    private Integer sortOrder = 0;

    @UiField(label = "Active", type = "boolean", sortable = true, order = 7)
    private Boolean active = true;

    @UiField(label = "Description", type = "textarea", order = 8)
    private String description;

    public String getDisplayValue() {
        return code != null ? code + " - " + name : name;
    }

    public static String getEmptyRecord() {
        return "{\n" +
                "    \"code\": '',\n" +
                "    \"name\": '',\n" +
                "    \"category\": 'REVENUE',\n" +
                "    \"subcategory\": '',\n" +
                "    \"sortOrder\": 0,\n" +
                "    \"active\": true,\n" +
                "    \"description\": ''\n" +
                "}";
    }
}
