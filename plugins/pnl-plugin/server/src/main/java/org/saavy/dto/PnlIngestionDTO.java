package org.saavy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.reference.PnlAccountCategory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PnlIngestionDTO implements Serializable {
    private LocalDate date;
    private String accountCode;
    private String accountName;
    private PnlAccountCategory category;
    private String subcategory;
    private BigDecimal amount;
    private String currency;
    private String entity;
    private String referenceId;
    private String description;
}
