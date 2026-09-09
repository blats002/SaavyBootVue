package org.saavy.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiDetail;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;
import org.saavy.reference.InvoiceStatus;
import org.saavy.reference.InvoiceType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(title = "Customer Invoices",
        dialogHeader = "Invoice Detail",
        optionLabel = "displayValue",
        messages = "{\"created\":\"Invoice Created\"" +
                ",\"updated\":\"Invoice Updated\"" +
                ",\"deleted\":\"Invoice Deleted\"" +
                ",\"deletedMany\":\"Invoices Deleted\"" +
                "}",
        masterEndPoint = "customer-invoices"
)
public class CustomerInvoiceDTO implements Serializable {
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    Long id;

    @UiField(label = "Invoice Number", type = "text", order = 2)
    public String invoiceNumber;

    @UiField(
            label = "Customer",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 3,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "parties"
    )
    public PartyDTO party;

    @UiField(label = "Invoice Status"
            , type = "enum"
            , order = 4
            , enumOptions = InvoiceStatus.getEnumOptions
            , editable = false)
    private InvoiceStatus invoiceStatus;

    @UiField(label = "Invoice Date", type = "date", order = 5)
    public LocalDate invoiceDate;

    @UiField(label = "Due Date", type = "date", order = 6)
    public LocalDate dueDate;

    @UiField(label = "Total Amount", type = "number", order = 7)
    public BigDecimal totalAmount;

    @UiField(label = "Outstanding Amount", type = "number", order = 8, editable = false)
    public BigDecimal outstandingAmount;

    @UiDetail(
            key = "customerFiles",
            title = "Files",
            parentField = "invoice",
            messages =  "{\"created\":\"File Created\"" +
                    ",\"updated\":\"File Updated\"" +
                    ",\"deleted\":\"File Deleted\"" +
                    ",\"deletedMany\":\"Files Deleted\"" +
                    "}",
            detailEndpoint  = "invoice-files"
    )
    @JsonManagedReference
    public List<CustomerInvoiceFileDTO> files;

    @UiDetail(
            key = "customerPayments",
            title = "Payments",
            parentField = "invoice",
            messages =  "{\"created\":\"Payment Created\"" +
                    ",\"updated\":\"Payment Updated\"" +
                    ",\"deleted\":\"Payment Deleted\"" +
                    ",\"deletedMany\":\"Payments Deleted\"" +
                    "}",
            detailEndpoint  = "payments"
    )
    public List<CustomerPaymentDTO> payments;

    public static String getEmptyRecord(){
        return "{\n" +
                "    \"invoiceNumber\": '',\n" +
                "    \"party\": null,\n" +
                "    \"invoiceStatus\": 'AWAITING_PAYMENT',\n" +
                "    \"invoiceDate\": null,\n" +
                "    \"dueDate\": null,\n" +
                "    \"totalAmount\": 0,\n" +
                "    \"outstandingAmount\": 0\n" +
                "}";
    }
}
