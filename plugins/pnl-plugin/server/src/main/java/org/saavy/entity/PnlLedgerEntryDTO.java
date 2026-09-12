package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(
        title = "P&L Ledger Entries",
        dialogHeader = "Ledger Entry Details",
        optionLabel = "description",
        messages = "{\"created\":\"Ledger Entry Created\"" +
                ",\"updated\":\"Ledger Entry Updated\"" +
                ",\"deleted\":\"Ledger Entry Deleted\"" +
                ",\"deletedMany\":\"Ledger Entries Deleted\"" +
                "}",
        masterEndPoint = "pnl-ledger-entries"
)
public class PnlLedgerEntryDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(
            label = "Account",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 2,
            optionLabel = "displayValue",
            optionValue = "id",
            optionsEndpoint = "pnl-accounts"
    )
    private PnlAccountDTO account;

    @UiField(label = "Entry Date", type = "date", required = true, sortable = true, order = 3)
    private LocalDate entryDate;

    @UiField(label = "Amount", type = "number", required = true, sortable = true, order = 4)
    private BigDecimal amount;

    @UiField(label = "Currency", type = "text", sortable = true, order = 5)
    private String currency = "USD";

    @UiField(label = "Entity", type = "text", sortable = true, order = 6)
    private String entityName = "Main";

    @UiField(label = "Reference ID", type = "text", sortable = true, order = 7)
    private String referenceId;

    @UiField(label = "Description / Memo", type = "text", sortable = true, order = 8)
    private String description;

    @UiField(label = "Created At", type = "datetime", hidden = true, editable = false, order = 9)
    private LocalDateTime createdAt;

    public String getDisplayValue() {
        return (referenceId != null ? referenceId + " - " : "") + (description != null ? description : "");
    }

    public static String getEmptyRecord() {
        return "{\n" +
                "    \"account\": null,\n" +
                "    \"entryDate\": null,\n" +
                "    \"amount\": 0,\n" +
                "    \"currency\": 'USD',\n" +
                "    \"entityName\": 'Main',\n" +
                "    \"referenceId\": '',\n" +
                "    \"description\": ''\n" +
                "}";
    }
}
