package org.saavy.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorPaymentDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    protected Long id;

    @UiField(
            label = "Invoice",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 2,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "invoices"
    )
    @JsonBackReference
    protected VendorInvoiceDTO invoice;

    @UiField(label = "File Name", type = "text", order = 3)
    String fileName;

    @UiField(label = "Content Type", hidden = true, type = "text", order = 4)
    String contentType;

    @UiField(
            label = "File",
            type = "file",
            fileNameField = "fileName",
            contentTypeField = "contentType"
//            accept = "image/png,image/jpeg",
//            maxSize = 2000000
    )
    String content;

    @UiField(label = "Amount", type = "number", order = 5)
    protected BigDecimal amount;

    @UiField(label = "Payment Date", type = "date", order = 6)
    protected LocalDate paymentDate;

    @UiField(label = "Reference", type = "text", order = 7)
    protected String reference;

    public static String getEmptyRecord(){
        return "{\n" +
                "    \"amount\": 0,\n" +
                "    \"paymentDate\": null,\n" +
                "    \"reference\": '',\n" +
                "    \"invoice\": null\n" +
                "}";
    }
}

