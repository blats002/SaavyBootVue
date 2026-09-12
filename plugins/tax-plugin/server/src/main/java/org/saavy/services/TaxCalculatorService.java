package org.saavy.services;

import org.saavy.dto.*;
import org.saavy.reference.BusinessType;
import org.saavy.reference.TaxRegime;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class TaxCalculatorService {

    private static final BigDecimal THRESHOLD_8_PERCENT_ALLOWANCE = new BigDecimal("250000");
    private static final BigDecimal VAT_THRESHOLD = new BigDecimal("3000000"); // ₱3M VAT Threshold
    private static final BigDecimal CREATE_MSME_NET_INCOME_CAP = new BigDecimal("5000000"); // ₱5M Net Income Cap

    /**
     * Computes Graduated Individual Income Tax Rates (under TRAIN Act)
     */
    public BigDecimal calculateGraduatedTax(BigDecimal taxableIncome) {
        if (taxableIncome == null || taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        double income = taxableIncome.doubleValue();
        double tax = 0.0;

        if (income <= 250000) {
            tax = 0.0;
        } else if (income <= 400000) {
            tax = (income - 250000) * 0.15;
        } else if (income <= 800000) {
            tax = 22500 + (income - 400000) * 0.20;
        } else if (income <= 2000000) {
            tax = 102500 + (income - 800000) * 0.25;
        } else if (income <= 8000000) {
            tax = 402500 + (income - 2000000) * 0.30;
        } else {
            tax = 2202500 + (income - 8000000) * 0.35;
        }

        return BigDecimal.valueOf(tax).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Simulates and compares all tax regimes for Sole Proprietorship / Freelancers
     */
    public TaxSimulationResultDTO simulateSoleProprietorTax(
            int year,
            BigDecimal grossRevenue,
            BigDecimal costOfGoodsSold,
            BigDecimal operatingExpenses,
            BigDecimal totalCwt2307,
            Map<String, BigDecimal> birSchedule2Deductions
    ) {
        grossRevenue = grossRevenue != null ? grossRevenue : BigDecimal.ZERO;
        costOfGoodsSold = costOfGoodsSold != null ? costOfGoodsSold : BigDecimal.ZERO;
        operatingExpenses = operatingExpenses != null ? operatingExpenses : BigDecimal.ZERO;
        totalCwt2307 = totalCwt2307 != null ? totalCwt2307 : BigDecimal.ZERO;

        BigDecimal grossProfit = grossRevenue.subtract(costOfGoodsSold);
        BigDecimal netProfitBeforeTax = grossProfit.subtract(operatingExpenses);

        List<TaxRegimeComparisonDTO> comparisons = new ArrayList<>();

        // Option 1: 8% Flat Gross Income Tax
        BigDecimal taxable8 = grossRevenue.subtract(THRESHOLD_8_PERCENT_ALLOWANCE);
        if (taxable8.compareTo(BigDecimal.ZERO) < 0) taxable8 = BigDecimal.ZERO;
        BigDecimal tax8Due = taxable8.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pct8Tax = BigDecimal.ZERO; // Exempt from 3% Percentage Tax under 8% regime
        BigDecimal total8 = tax8Due.add(pct8Tax);
        BigDecimal netPayable8 = total8.subtract(totalCwt2307).max(BigDecimal.ZERO);

        comparisons.add(TaxRegimeComparisonDTO.builder()
                .regime(TaxRegime.FLAT_8_PERCENT)
                .regimeName("8% Flat Gross Income Tax")
                .description("Flat 8% tax on gross sales/receipts exceeding ₱250,000. 100% EXEMPT from 3% Percentage Tax.")
                .grossRevenue(grossRevenue)
                .allowableDeductions(THRESHOLD_8_PERCENT_ALLOWANCE)
                .taxableIncome(taxable8)
                .incomeTaxDue(tax8Due)
                .percentageTax2551Q(pct8Tax)
                .totalTaxLiability(total8)
                .cwt2307Credits(totalCwt2307)
                .netTaxPayable(netPayable8)
                .highlights(List.of(
                        "No receipt substantiation required",
                        "Exempt from 3% Quarterly Percentage Tax (2551Q)",
                        "Simplified single-page BIR Form 1701A filing"
                ))
                .build());

        // Option 2: 40% Optional Standard Deduction (OSD)
        BigDecimal osdDeduction = grossRevenue.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxableOsd = grossRevenue.subtract(osdDeduction);
        BigDecimal taxOsdDue = calculateGraduatedTax(taxableOsd);
        BigDecimal pctOsdTax = grossRevenue.compareTo(VAT_THRESHOLD) <= 0
                ? grossRevenue.multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal totalOsd = taxOsdDue.add(pctOsdTax);
        BigDecimal netPayableOsd = totalOsd.subtract(totalCwt2307).max(BigDecimal.ZERO);

        comparisons.add(TaxRegimeComparisonDTO.builder()
                .regime(TaxRegime.OSD_40_PERCENT)
                .regimeName("40% Optional Standard Deduction (OSD)")
                .description("Standard 40% deduction from gross revenue without needing receipts, taxed at graduated rates.")
                .grossRevenue(grossRevenue)
                .allowableDeductions(osdDeduction)
                .taxableIncome(taxableOsd)
                .incomeTaxDue(taxOsdDue)
                .percentageTax2551Q(pctOsdTax)
                .totalTaxLiability(totalOsd)
                .cwt2307Credits(totalCwt2307)
                .netTaxPayable(netPayableOsd)
                .highlights(List.of(
                        "Automatic 40% standard expense deduction",
                        "Audited Financial Statements not strictly required",
                        "Includes 3% Percentage Tax if non-VAT"
                ))
                .build());

        // Option 3: Graduated Itemized Deductions (Actual Audited OPEX)
        BigDecimal totalItemizedDeductions = costOfGoodsSold.add(operatingExpenses);
        BigDecimal taxableItemized = grossRevenue.subtract(totalItemizedDeductions).max(BigDecimal.ZERO);
        BigDecimal taxItemizedDue = calculateGraduatedTax(taxableItemized);
        BigDecimal pctItemizedTax = grossRevenue.compareTo(VAT_THRESHOLD) <= 0
                ? grossRevenue.multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal totalItemized = taxItemizedDue.add(pctItemizedTax);
        BigDecimal netPayableItemized = totalItemized.subtract(totalCwt2307).max(BigDecimal.ZERO);

        comparisons.add(TaxRegimeComparisonDTO.builder()
                .regime(TaxRegime.ITEMIZED_DEDUCTIONS)
                .regimeName("Itemized Deductions (Graduated Rates)")
                .description("Actual audited COGS and allowable operating expense deductions, taxed at graduated rates.")
                .grossRevenue(grossRevenue)
                .allowableDeductions(totalItemizedDeductions)
                .taxableIncome(taxableItemized)
                .incomeTaxDue(taxItemizedDue)
                .percentageTax2551Q(pctItemizedTax)
                .totalTaxLiability(totalItemized)
                .cwt2307Credits(totalCwt2307)
                .netTaxPayable(netPayableItemized)
                .highlights(List.of(
                        "Deducts 100% of verified receipts and business expenses",
                        "Best for high-overhead or lower-margin businesses",
                        "Requires complete BIR-compliant official receipts"
                ))
                .build());

        // Find optimal regime (lowest total tax liability)
        TaxRegimeComparisonDTO optimal = comparisons.stream()
                .min(Comparator.comparing(TaxRegimeComparisonDTO::getTotalTaxLiability))
                .orElse(comparisons.get(0));

        BigDecimal highestTax = comparisons.stream()
                .map(TaxRegimeComparisonDTO::getTotalTaxLiability)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        for (TaxRegimeComparisonDTO c : comparisons) {
            c.setRecommended(c.getRegime() == optimal.getRegime());
            BigDecimal savings = highestTax.subtract(c.getTotalTaxLiability()).max(BigDecimal.ZERO);
            c.setEstimatedSavingsVsHighest(savings);
        }

        BigDecimal maxSavings = highestTax.subtract(optimal.getTotalTaxLiability()).max(BigDecimal.ZERO);

        String summary = String.format("By selecting the %s, your business saves approximately ₱%,.2f in total taxes for %d.",
                optimal.getRegimeName(), maxSavings, year);

        return TaxSimulationResultDTO.builder()
                .taxYear(year)
                .businessType(BusinessType.SOLE_PROPRIETOR)
                .businessTypeName(BusinessType.SOLE_PROPRIETOR.getLabel())
                .grossRevenue(grossRevenue)
                .costOfGoodsSold(costOfGoodsSold)
                .grossProfit(grossProfit)
                .operatingExpenses(operatingExpenses)
                .netProfitBeforeTax(netProfitBeforeTax)
                .totalCwt2307Credits(totalCwt2307)
                .regimeComparisons(comparisons)
                .recommendedRegime(optimal.getRegimeName())
                .maxEstimatedSavings(maxSavings)
                .recommendationSummary(summary)
                .birSchedule2Deductions(birSchedule2Deductions != null ? birSchedule2Deductions : Collections.emptyMap())
                .build();
    }

    /**
     * Computes CREATE Act Corporate Income Tax (Form 1702-RT)
     */
    public BirForm1702RtDTO calculateCorporateTax(
            int year,
            BigDecimal grossSales,
            BigDecimal costOfSales,
            BigDecimal operatingExpenses,
            BigDecimal totalCwt2307,
            Map<String, BigDecimal> schedule1CostOfSales,
            Map<String, BigDecimal> schedule2ItemizedDeductions
    ) {
        grossSales = grossSales != null ? grossSales : BigDecimal.ZERO;
        costOfSales = costOfSales != null ? costOfSales : BigDecimal.ZERO;
        operatingExpenses = operatingExpenses != null ? operatingExpenses : BigDecimal.ZERO;
        totalCwt2307 = totalCwt2307 != null ? totalCwt2307 : BigDecimal.ZERO;

        BigDecimal grossIncome = grossSales.subtract(costOfSales);
        BigDecimal taxableIncome = grossIncome.subtract(operatingExpenses).max(BigDecimal.ZERO);

        // CREATE Act tier: 20% if Taxable Net Income <= ₱5M; else 25%
        BigDecimal ratePercent = taxableIncome.compareTo(CREATE_MSME_NET_INCOME_CAP) <= 0
                ? new BigDecimal("20.00")
                : new BigDecimal("25.00");

        String rateType = ratePercent.compareTo(new BigDecimal("20.00")) == 0 ? "MSME_20" : "REGULAR_25";

        BigDecimal rcit = taxableIncome.multiply(ratePercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal mcit = grossIncome.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP); // 2% MCIT on Gross Income

        // Income tax due is higher of RCIT or MCIT
        BigDecimal incomeTaxDue = rcit.max(mcit);
        BigDecimal netTaxPayable = incomeTaxDue.subtract(totalCwt2307).max(BigDecimal.ZERO);

        return BirForm1702RtDTO.builder()
                .formNumber("1702-RT")
                .taxYear(year)
                .registeredName("Saavy Client Corporation")
                .tin("000-123-456-000")
                .rdoCode("044")
                .corporateRateType(rateType)
                .corporateRatePercent(ratePercent)
                .grossSales(grossSales)
                .costOfSales(costOfSales)
                .grossIncome(grossIncome)
                .allowableDeductions(operatingExpenses)
                .taxableIncome(taxableIncome)
                .regularCorporateIncomeTax(rcit)
                .minimumCorporateIncomeTax(mcit)
                .incomeTaxDue(incomeTaxDue)
                .creditableTaxWithheld2307(totalCwt2307)
                .netTaxPayable(netTaxPayable)
                .schedule1CostOfSales(schedule1CostOfSales != null ? schedule1CostOfSales : Collections.emptyMap())
                .schedule2ItemizedDeductions(schedule2ItemizedDeductions != null ? schedule2ItemizedDeductions : Collections.emptyMap())
                .build();
    }
}
