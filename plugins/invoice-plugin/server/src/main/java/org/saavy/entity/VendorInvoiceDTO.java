package org.saavy.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiDetail;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;
import org.saavy.reference.InvoiceStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(title = "Vendor Invoices & Bills",
        dialogHeader = "Invoice Detail",
        optionLabel = "displayValue",
        messages = "{\"created\":\"Invoice Created\",\"updated\":\"Invoice Updated\",\"deleted\":\"Invoice Deleted\",\"deletedMany\":\"Invoices Deleted\"}",
        masterEndPoint = "vendor-invoices"
)
public class VendorInvoiceDTO implements Serializable {
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    Long id;

    @UiField(label = "Invoice / Bill Number", type = "text", order = 2)
    public String invoiceNumber;

    @UiField(
            label = "Vendor",
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

    @UiField(
            label = "Tax Bucket (BIR Schedule)",
            type = "manyToOne",
            editable = true,
            required = false,
            sortable = true,
            order = 4,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "tax-categories"
    )
    public TaxCategoryDTO taxCategory;

    @UiField(label = "Invoice Status"
            , type = "enum"
            , order = 5
            , enumOptions = InvoiceStatus.getEnumOptions
            , editable = false)
    private InvoiceStatus invoiceStatus;

    @UiField(label = "Invoice Date", type = "date", order = 6)
    public LocalDate invoiceDate;

    @UiField(label = "Due Date", type = "date", order = 7)
    public LocalDate dueDate;

    @UiField(label = "Total Amount", type = "number", order = 8)
    public BigDecimal totalAmount;

    @UiField(label = "CWT 2307 Withheld", type = "number", order = 9)
    public BigDecimal cwt2307Amount = BigDecimal.ZERO;

    @UiField(label = "VAT Status", type = "enum", order = 10, enumOptions = "[{\"label\":\"VAT Inclusive (12%)\",\"value\":\"VAT_INCLUSIVE\"},{\"label\":\"VAT Exclusive\",\"value\":\"VAT_EXCLUSIVE\"},{\"label\":\"Non-VAT / Percentage Tax (3%)\",\"value\":\"NON_VAT\"},{\"label\":\"VAT Exempt\",\"value\":\"VAT_EXEMPT\"}]")
    public String vatStatus = "VAT_INCLUSIVE";

    @UiField(label = "Tax Deductible", type = "boolean", order = 11)
    public Boolean isDeductible = true;

    @UiField(label = "Outstanding Amount", type = "number", order = 12, editable = false)
    public BigDecimal outstandingAmount;

    @UiDetail(
            key = "vendorFiles",
            title = "Receipts & Files",
            parentField = "invoice",
            messages = "{\"created\":\"File Created\",\"updated\":\"File Updated\",\"deleted\":\"File Deleted\",\"deletedMany\":\"Files Deleted\"}",
            detailEndpoint = "invoice-files"
    )
    @JsonManagedReference
    public List<VendorInvoiceFileDTO> files;

    @UiDetail(
            key = "vendorPayments",
            title = "Payments",
            parentField = "invoice",
            messages = "{\"created\":\"Payment Created\",\"updated\":\"Payment Updated\",\"deleted\":\"Payment Deleted\",\"deletedMany\":\"Payments Deleted\"}",
            detailEndpoint = "payments"
    )
    public List<VendorPaymentDTO> payments;

    public static String getEmptyRecord(){
        return "{\n" +
                "    \"invoiceNumber\": '',\n" +
                "    \"party\": null,\n" +
                "    \"taxCategory\": null,\n" +
                "    \"invoiceStatus\": 'RECEIVED',\n" +
                "    \"invoiceDate\": null,\n" +
                "    \"dueDate\": null,\n" +
                "    \"totalAmount\": 0,\n" +
                "    \"cwt2307Amount\": 0,\n" +
                "    \"vatStatus\": 'VAT_INCLUSIVE',\n" +
                "    \"isDeductible\": true,\n" +
                "    \"outstandingAmount\": 0\n" +
                "}";
    }
}
