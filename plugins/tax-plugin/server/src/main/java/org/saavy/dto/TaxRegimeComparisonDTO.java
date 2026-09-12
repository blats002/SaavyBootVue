package org.saavy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.reference.TaxRegime;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRegimeComparisonDTO {
    private TaxRegime regime;
    private String regimeName;
    private String description;
    private BigDecimal grossRevenue;
    private BigDecimal allowableDeductions;
    private BigDecimal taxableIncome;
    private BigDecimal incomeTaxDue;
    private BigDecimal percentageTax2551Q; // 3% if applicable
    private BigDecimal totalTaxLiability; // Income Tax + Percentage Tax
    private BigDecimal cwt2307Credits;
    private BigDecimal netTaxPayable;
    private BigDecimal estimatedSavingsVsHighest;
    private boolean isRecommended;
    private List<String> highlights;
}
