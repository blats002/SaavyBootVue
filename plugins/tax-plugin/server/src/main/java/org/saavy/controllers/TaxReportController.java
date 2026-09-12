package org.saavy.controllers;

import org.saavy.dto.BirForm1701ADTO;
import org.saavy.dto.BirForm1702RtDTO;
import org.saavy.dto.TaxSimulationResultDTO;
import org.saavy.services.TaxCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tax")
public class TaxReportController {

    @Autowired
    private TaxCalculatorService taxCalculatorService;

    @GetMapping("/simulate")
    public ResponseEntity<TaxSimulationResultDTO> simulate(
            @RequestParam(defaultValue = "2025") int year,
            @RequestParam(required = false) BigDecimal grossRevenue,
            @RequestParam(required = false) BigDecimal cogs,
            @RequestParam(required = false) BigDecimal opex,
            @RequestParam(required = false) BigDecimal cwt2307
    ) {
        // Default seed figures if not passed
        BigDecimal rev = grossRevenue != null ? grossRevenue : new BigDecimal("1850000.00");
        BigDecimal cost = cogs != null ? cogs : new BigDecimal("420000.00");
        BigDecimal exp = opex != null ? opex : new BigDecimal("510000.00");
        BigDecimal cwt = cwt2307 != null ? cwt2307 : new BigDecimal("37000.00"); // 2% CWT on service fees

        Map<String, BigDecimal> schedule2 = new LinkedHashMap<>();
        schedule2.put("Schedule 2 Line 1 - Salaries & Wages", new BigDecimal("180000.00"));
        schedule2.put("Schedule 2 Line 2 - SSS, PhilHealth, HDMF", new BigDecimal("24000.00"));
        schedule2.put("Schedule 2 Line 3 - Rent", new BigDecimal("120000.00"));
        schedule2.put("Schedule 2 Line 4 - Utilities & Communication", new BigDecimal("65000.00"));
        schedule2.put("Schedule 2 Line 6 - Professional Fees", new BigDecimal("45000.00"));
        schedule2.put("Schedule 2 Line 7 - Representation & Entertainment", new BigDecimal("9250.00")); // Within 0.5% cap
        schedule2.put("Schedule 2 Line 8 - Taxes & Licenses", new BigDecimal("18500.00"));
        schedule2.put("Schedule 2 Line 10 - Office Supplies", new BigDecimal("28250.00"));
        schedule2.put("Schedule 2 Line 11 - Travel & Transportation", new BigDecimal("20000.00"));

        TaxSimulationResultDTO result = taxCalculatorService.simulateSoleProprietorTax(year, rev, cost, exp, cwt, schedule2);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/form-1701a")
    public ResponseEntity<BirForm1701ADTO> getForm1701A(
            @RequestParam(defaultValue = "2025") int year,
            @RequestParam(defaultValue = "8_PERCENT") String method
    ) {
        BigDecimal grossRevenue = new BigDecimal("1850000.00");
        BigDecimal cwt = new BigDecimal("37000.00");

        BigDecimal deduction = method.equals("8_PERCENT") ? new BigDecimal("250000.00") : new BigDecimal("740000.00");
        BigDecimal taxable = grossRevenue.subtract(deduction).max(BigDecimal.ZERO);
        BigDecimal taxDue = method.equals("8_PERCENT")
                ? taxable.multiply(new BigDecimal("0.08"))
                : taxCalculatorService.calculateGraduatedTax(taxable);

        BigDecimal netPayable = taxDue.subtract(cwt).max(BigDecimal.ZERO);

        BirForm1701ADTO form = BirForm1701ADTO.builder()
                .formNumber("1701A")
                .taxYear(year)
                .taxpayerName("Juan Dela Cruz / Saavy Freelance Services")
                .tin("123-456-789-000")
                .rdoCode("044")
                .lineOfBusiness("Information Technology Consultancy")
                .selectedMethod(method)
                .grossSalesReceipts(grossRevenue)
                .allowableReduction(deduction)
                .taxableIncome(taxable)
                .taxRate(method.equals("8_PERCENT") ? new BigDecimal("8.00") : null)
                .incomeTaxDue(taxDue)
                .creditableTaxWithheld2307(cwt)
                .totalTaxCredits(cwt)
                .netTaxPayable(netPayable)
                .overpaymentRefundOrCredit(BigDecimal.ZERO)
                .build();

        return ResponseEntity.ok(form);
    }

    @GetMapping("/form-1702rt")
    public ResponseEntity<BirForm1702RtDTO> getForm1702RT(@RequestParam(defaultValue = "2025") int year) {
        BigDecimal grossSales = new BigDecimal("4500000.00");
        BigDecimal costOfSales = new BigDecimal("1800000.00");
        BigDecimal opex = new BigDecimal("1200000.00");
        BigDecimal cwt = new BigDecimal("90000.00");

        Map<String, BigDecimal> sch1 = Map.of(
                "Direct Materials", new BigDecimal("1200000.00"),
                "Direct Labor", new BigDecimal("600000.00")
        );

        Map<String, BigDecimal> sch2 = Map.of(
                "Salaries & Benefits", new BigDecimal("500000.00"),
                "Rent", new BigDecimal("300000.00"),
                "Utilities", new BigDecimal("150000.00"),
                "Professional Fees", new BigDecimal("100000.00"),
                "Taxes & Licenses", new BigDecimal("50000.00"),
                "Depreciation", new BigDecimal("100000.00")
        );

        BirForm1702RtDTO form = taxCalculatorService.calculateCorporateTax(year, grossSales, costOfSales, opex, cwt, sch1, sch2);
        return ResponseEntity.ok(form);
    }
}
