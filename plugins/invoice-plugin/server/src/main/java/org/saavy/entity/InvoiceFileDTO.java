package org.saavy.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.saavy.component.UiField;
import org.saavy.reference.InvoiceStatus;
import org.saavy.reference.InvoiceType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link InvoiceFile}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceFileDTO implements Serializable {
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    Long id;

    @UiField(label = "File Name", type = "text", order = 2)
    String fileName;

    @UiField(label = "Content Type", hidden = true, type = "text", order = 3)
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

    @UiField(
            label = "Invoice",
            type = "manyToOne",
            editable = true,
            required = true,
            sortable = true,
            order = 5,
            optionLabel = "name",
            optionValue = "id",
            optionsEndpoint = "invoices"
    )
    @JsonBackReference
    InvoiceDTO invoice;
}