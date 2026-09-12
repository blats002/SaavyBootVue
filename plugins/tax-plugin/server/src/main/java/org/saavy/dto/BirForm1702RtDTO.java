package org.saavy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirForm1702RtDTO {
    private String formNumber; // "1702-RT"
    private int taxYear;
    private String registeredName;
    private String tin;
    private String rdoCode;
    private String corporateRateType; // "MSME_20" or "REGULAR_25"
    private BigDecimal corporateRatePercent; // 20% or 25%

    // Income Schedule
    private BigDecimal grossSales;
    private BigDecimal costOfSales;
    private BigDecimal grossIncome;
    private BigDecimal allowableDeductions;
    private BigDecimal taxableIncome;

    // Tax Computation
    private BigDecimal regularCorporateIncomeTax; // RCIT (20% or 25%)
    private BigDecimal minimumCorporateIncomeTax; // MCIT (2% of Gross Income)
    private BigDecimal incomeTaxDue; // Higher of RCIT or MCIT
    private BigDecimal creditableTaxWithheld2307;
    private BigDecimal netTaxPayable;

    private Map<String, BigDecimal> schedule1CostOfSales;
    private Map<String, BigDecimal> schedule2ItemizedDeductions;
}
