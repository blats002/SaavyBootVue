package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;
import org.saavy.reference.PartyType;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(
        title = "Parties",
        dialogHeader = "Party Details",
        optionLabel = "name",
        messages = "{\"created\":\"Party Created\"" +
                ",\"updated\":\"Party Updated\"" +
                ",\"deleted\":\"Party Deleted\"" +
                ",\"deletedMany\":\"Parties Deleted\"" +
                "}",
        masterEndPoint = "parties"
)
public class PartyDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "Party Name", type = "text", required = true, sortable = true, order = 2)
    private String name;

    @UiField(label = "Party Type", type = "enum", enumOptions = PartyType.getEnumOptions, sortable = true, order = 3)
    private PartyType partyType;

    @UiField(label = "Email", type = "text", sortable = true, order = 4)
    private String email;

    @UiField(label = "Phone", type = "text", order = 5)
    private String phone;

    @UiField(label = "Tax ID / VAT", type = "text", order = 6)
    private String taxId;

    @UiField(label = "Address", type = "text", order = 7)
    private String address;

    @UiField(label = "City", type = "text", sortable = true, order = 8)
    private String city;

    @UiField(label = "State / Province", type = "text", order = 9)
    private String state;

    @UiField(label = "Postal Code", type = "text", order = 10)
    private String postalCode;

    @UiField(label = "Country", type = "text", sortable = true, order = 11)
    private String country;

    @UiField(label = "Notes", type = "textarea", order = 12)
    private String notes;

    public String getDisplayValue() {
        return name;
    }

    public static String getEmptyRecord() {
        return "{\n" +
                "    \"name\": '',\n" +
                "    \"partyType\": 'CUSTOMER',\n" +
                "    \"email\": '',\n" +
                "    \"phone\": '',\n" +
                "    \"taxId\": '',\n" +
                "    \"address\": '',\n" +
                "    \"city\": '',\n" +
                "    \"state\": '',\n" +
                "    \"postalCode\": '',\n" +
                "    \"country\": '',\n" +
                "    \"notes\": ''\n" +
                "}";
    }
}
