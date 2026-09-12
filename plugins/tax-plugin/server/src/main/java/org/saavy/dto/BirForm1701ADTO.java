package org.saavy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirForm1701ADTO {
    private String formNumber; // "1701A"
    private int taxYear;
    private String taxpayerName;
    private String tin;
    private String rdoCode;
    private String lineOfBusiness;
    private String selectedMethod; // "8_PERCENT", "OSD", "ITEMIZED"

    // Part IV - Computation of Tax
    private BigDecimal grossSalesReceipts;
    private BigDecimal allowableReduction; // 250,000 for 8%
    private BigDecimal taxableIncome;
    private BigDecimal taxRate;
    private BigDecimal incomeTaxDue;
    private BigDecimal priorQuarterPayments;
    private BigDecimal creditableTaxWithheld2307;
    private BigDecimal totalTaxCredits;
    private BigDecimal netTaxPayable;
    private BigDecimal overpaymentRefundOrCredit;

    // Itemized Schedule Details (if selected)
    private Map<String, BigDecimal> itemizedDeductions;
}
