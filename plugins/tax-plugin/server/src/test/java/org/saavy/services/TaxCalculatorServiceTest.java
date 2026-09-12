package org.saavy.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.saavy.dto.BirForm1702RtDTO;
import org.saavy.dto.TaxRegimeComparisonDTO;
import org.saavy.dto.TaxSimulationResultDTO;
import org.saavy.reference.TaxRegime;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TaxCalculatorServiceTest {

    private TaxCalculatorService taxCalculatorService;

    @BeforeEach
    void setUp() {
        taxCalculatorService = new TaxCalculatorService();
    }

    @Test
    @DisplayName("Graduated Tax Brackets under TRAIN Act")
    void testGraduatedTax() {
        // <= ₱250k: 0%
        assertEquals(0, new BigDecimal("0.00").compareTo(taxCalculatorService.calculateGraduatedTax(new BigDecimal("250000"))));

        // ₱400k: (400k - 250k) * 15% = 22,500
        assertEquals(0, new BigDecimal("22500.00").compareTo(taxCalculatorService.calculateGraduatedTax(new BigDecimal("400000"))));

        // ₱600k: 22,500 + (600k - 400k) * 20% = 22,500 + 40,000 = 62,500
        assertEquals(0, new BigDecimal("62500.00").compareTo(taxCalculatorService.calculateGraduatedTax(new BigDecimal("600000"))));
    }

    @Test
    @DisplayName("Sole Proprietorship 8% Flat vs OSD vs Itemized Simulation")
    void testSoleProprietorSimulation() {
        BigDecimal gross = new BigDecimal("1850000.00");
        BigDecimal cogs = new BigDecimal("420000.00");
        BigDecimal opex = new BigDecimal("510000.00");
        BigDecimal cwt = new BigDecimal("37000.00");

        TaxSimulationResultDTO result = taxCalculatorService.simulateSoleProprietorTax(
                2025, gross, cogs, opex, cwt, Collections.emptyMap()
        );

        assertNotNull(result);
        assertEquals(3, result.getRegimeComparisons().size());

        // 8% Flat: (1,850,000 - 250,000) * 8% = 1,600,000 * 8% = 128,000
        TaxRegimeComparisonDTO flat8 = result.getRegimeComparisons().stream()
                .filter(r -> r.getRegime() == TaxRegime.FLAT_8_PERCENT)
                .findFirst()
                .orElseThrow();
        assertEquals(0, new BigDecimal("128000.00").compareTo(flat8.getIncomeTaxDue()));
        assertEquals(0, BigDecimal.ZERO.compareTo(flat8.getPercentageTax2551Q()));
        assertEquals(0, new BigDecimal("91000.00").compareTo(flat8.getNetTaxPayable())); // 128k - 37k CWT = 91k

        // Recommendation should be populated
        assertNotNull(result.getRecommendedRegime());
        assertTrue(result.getMaxEstimatedSavings().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("CREATE Act Corporate Tax (20% MSME Tier vs 2% MCIT)")
    void testCorporateTaxCreateAct() {
        BigDecimal grossSales = new BigDecimal("4500000.00");
        BigDecimal costOfSales = new BigDecimal("1800000.00");
        BigDecimal opex = new BigDecimal("1200000.00");
        BigDecimal cwt = new BigDecimal("90000.00");

        BirForm1702RtDTO form = taxCalculatorService.calculateCorporateTax(
                2025, grossSales, costOfSales, opex, cwt, Collections.emptyMap(), Collections.emptyMap()
        );

        assertNotNull(form);
        // Gross Income = 4.5M - 1.8M = 2.7M
        assertEquals(0, new BigDecimal("2700000.00").compareTo(form.getGrossIncome()));
        // Taxable Net Income = 2.7M - 1.2M = 1.5M
        assertEquals(0, new BigDecimal("1500000.00").compareTo(form.getTaxableIncome()));
        // RCIT @ 20% MSME = 1.5M * 20% = 300,000
        assertEquals(0, new BigDecimal("300000.00").compareTo(form.getRegularCorporateIncomeTax()));
        // MCIT @ 2% on 2.7M = 54,000
        assertEquals(0, new BigDecimal("54000.00").compareTo(form.getMinimumCorporateIncomeTax()));
        // Tax due = max(300k, 54k) = 300,000
        assertEquals(0, new BigDecimal("300000.00").compareTo(form.getIncomeTaxDue()));
        // Net payable = 300k - 90k CWT = 210,000
        assertEquals(0, new BigDecimal("210000.00").compareTo(form.getNetTaxPayable()));
    }
}
