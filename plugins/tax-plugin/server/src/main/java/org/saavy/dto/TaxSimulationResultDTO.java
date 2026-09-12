package org.saavy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.reference.BusinessType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxSimulationResultDTO {
    private int taxYear;
    private BusinessType businessType;
    private String businessTypeName;
    private BigDecimal grossRevenue;
    private BigDecimal costOfGoodsSold;
    private BigDecimal grossProfit;
    private BigDecimal operatingExpenses;
    private BigDecimal netProfitBeforeTax;
    private BigDecimal totalCwt2307Credits;

    // Regimes compared
    private List<TaxRegimeComparisonDTO> regimeComparisons;

    // Recommendation
    private String recommendedRegime;
    private BigDecimal maxEstimatedSavings;
    private String recommendationSummary;

    // Itemized Schedule Breakdown (for BIR Sch 2)
    private Map<String, BigDecimal> birSchedule2Deductions;
}
