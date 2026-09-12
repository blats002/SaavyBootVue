package org.saavy.services;

import org.saavy.dto.PnlReportMatrixDTO;
import org.saavy.dto.PnlReportMatrixDTO.PnlReportRowDTO;
import org.saavy.dto.PnlReportMatrixDTO.PnlReportSectionDTO;
import org.saavy.entity.PnlAccount;
import org.saavy.entity.PnlAccountRepository;
import org.saavy.entity.PnlLedgerEntry;
import org.saavy.entity.PnlLedgerEntryRepository;
import org.saavy.reference.PnlAccountCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class PnlReportService {

    @Autowired
    private PnlAccountRepository pnlAccountRepository;

    @Autowired
    private PnlLedgerEntryRepository pnlLedgerEntryRepository;

    public PnlReportMatrixDTO generateReportMatrix(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<String> periods = new ArrayList<>();
        List<String> periodLabels = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String pKey = String.format("%d-%02d", year, m);
            periods.add(pKey);
            String monthName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            periodLabels.add(monthName + " " + year);
        }

        List<PnlAccount> accounts = pnlAccountRepository.findByActiveTrueOrderBySortOrderAscCodeAsc();
        List<PnlLedgerEntry> entries = pnlLedgerEntryRepository.findByEntryDateBetweenWithAccount(startDate, endDate);

        // Group entries by accountId and periodKey (YYYY-MM)
        Map<Long, Map<String, BigDecimal>> ledgerSums = new HashMap<>();
        for (PnlLedgerEntry entry : entries) {
            if (entry.getAccount() == null || entry.getAccount().getId() == null) continue;
            Long accId = entry.getAccount().getId();
            String pKey = String.format("%d-%02d", entry.getEntryDate().getYear(), entry.getEntryDate().getMonthValue());
            
            ledgerSums.computeIfAbsent(accId, k -> new HashMap<>())
                    .merge(pKey, entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO, BigDecimal::add);
        }

        // Build sections
        Map<PnlAccountCategory, List<PnlAccount>> categorizedAccounts = new EnumMap<>(PnlAccountCategory.class);
        for (PnlAccount acc : accounts) {
            categorizedAccounts.computeIfAbsent(acc.getCategory(), k -> new ArrayList<>()).add(acc);
        }

        PnlReportSectionDTO revenueSection = buildSection(PnlAccountCategory.REVENUE, "1. REVENUE / INCOME", categorizedAccounts.get(PnlAccountCategory.REVENUE), ledgerSums, periods);
        PnlReportSectionDTO cogsSection = buildSection(PnlAccountCategory.COGS, "2. COST OF GOODS SOLD (COGS)", categorizedAccounts.get(PnlAccountCategory.COGS), ledgerSums, periods);
        PnlReportSectionDTO opexSection = buildSection(PnlAccountCategory.OPEX, "3. OPERATING EXPENSES (OPEX)", categorizedAccounts.get(PnlAccountCategory.OPEX), ledgerSums, periods);
        PnlReportSectionDTO taxSection = buildSection(PnlAccountCategory.TAX, "4. TAXES & COMPLIANCE", categorizedAccounts.get(PnlAccountCategory.TAX), ledgerSums, periods);

        // Calculated Rows
        PnlReportRowDTO revenueTotalRow = revenueSection.getSubtotalRow();
        PnlReportRowDTO cogsTotalRow = cogsSection.getSubtotalRow();
        PnlReportRowDTO opexTotalRow = opexSection.getSubtotalRow();
        PnlReportRowDTO taxTotalRow = taxSection.getSubtotalRow();

        // Gross Profit = Revenue - COGS
        PnlReportRowDTO grossProfitRow = calculateDifferenceRow("Gross Profit", revenueTotalRow, cogsTotalRow, periods);
        // Gross Margin % = (Gross Profit / Revenue) * 100
        PnlReportRowDTO grossMarginPercentRow = calculatePercentageRow("Gross Margin %", grossProfitRow, revenueTotalRow, periods);

        // Operating Income / EBITDA = Gross Profit - OPEX
        PnlReportRowDTO operatingIncomeRow = calculateDifferenceRow("Operating Income (EBITDA)", grossProfitRow, opexTotalRow, periods);
        // Operating Margin % = (Operating Income / Revenue) * 100
        PnlReportRowDTO operatingMarginPercentRow = calculatePercentageRow("Operating Margin %", operatingIncomeRow, revenueTotalRow, periods);

        // Net Income = Operating Income - Tax
        PnlReportRowDTO netIncomeRow = calculateDifferenceRow("Net Income", operatingIncomeRow, taxTotalRow, periods);
        // Net Margin % = (Net Income / Revenue) * 100
        PnlReportRowDTO netMarginPercentRow = calculatePercentageRow("Net Margin %", netIncomeRow, revenueTotalRow, periods);

        List<PnlReportSectionDTO> allSections = new ArrayList<>();
        allSections.add(revenueSection);
        allSections.add(cogsSection);
        allSections.add(opexSection);
        allSections.add(taxSection);

        String detectedCurrency = entries.stream()
                .map(PnlLedgerEntry::getCurrency)
                .filter(c -> c != null && !c.isBlank())
                .findFirst()
                .orElse("PHP");

        return PnlReportMatrixDTO.builder()
                .year(year)
                .currency(detectedCurrency)
                .periods(periods)
                .periodLabels(periodLabels)
                .sections(allSections)
                .grossProfitRow(grossProfitRow)
                .grossMarginPercentRow(grossMarginPercentRow)
                .operatingIncomeRow(operatingIncomeRow)
                .operatingMarginPercentRow(operatingMarginPercentRow)
                .netIncomeRow(netIncomeRow)
                .netMarginPercentRow(netMarginPercentRow)
                .build();
    }

    private PnlReportSectionDTO buildSection(PnlAccountCategory category, String title, List<PnlAccount> accounts, Map<Long, Map<String, BigDecimal>> ledgerSums, List<String> periods) {
        List<PnlReportRowDTO> rows = new ArrayList<>();
        Map<String, BigDecimal> subtotalPeriods = new HashMap<>();
        BigDecimal sectionTotal = BigDecimal.ZERO;

        for (String p : periods) {
            subtotalPeriods.put(p, BigDecimal.ZERO);
        }

        if (accounts != null) {
            for (PnlAccount acc : accounts) {
                Map<String, BigDecimal> accPeriods = new HashMap<>();
                BigDecimal rowTotal = BigDecimal.ZERO;
                Map<String, BigDecimal> accSums = ledgerSums.getOrDefault(acc.getId(), Collections.emptyMap());

                for (String p : periods) {
                    BigDecimal val = accSums.getOrDefault(p, BigDecimal.ZERO);
                    accPeriods.put(p, val);
                    rowTotal = rowTotal.add(val);
                    subtotalPeriods.merge(p, val, BigDecimal::add);
                }
                sectionTotal = sectionTotal.add(rowTotal);

                rows.add(PnlReportRowDTO.builder()
                        .accountId(acc.getId())
                        .code(acc.getCode())
                        .name(acc.getName())
                        .subcategory(acc.getSubcategory())
                        .isCalculated(false)
                        .isHeader(false)
                        .isSubtotal(false)
                        .isPercentage(false)
                        .periodValues(accPeriods)
                        .totalValue(rowTotal)
                        .build());
            }
        }

        PnlReportRowDTO subtotalRow = PnlReportRowDTO.builder()
                .name("Total " + category.getLabel())
                .isCalculated(true)
                .isHeader(false)
                .isSubtotal(true)
                .isPercentage(false)
                .periodValues(subtotalPeriods)
                .totalValue(sectionTotal)
                .build();

        return PnlReportSectionDTO.builder()
                .sectionKey(category.name())
                .title(title)
                .category(category)
                .rows(rows)
                .subtotalRow(subtotalRow)
                .build();
    }

    private PnlReportRowDTO calculateDifferenceRow(String name, PnlReportRowDTO positiveRow, PnlReportRowDTO negativeRow, List<String> periods) {
        Map<String, BigDecimal> diffPeriods = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (String p : periods) {
            BigDecimal pos = positiveRow != null && positiveRow.getPeriodValues() != null ? positiveRow.getPeriodValues().getOrDefault(p, BigDecimal.ZERO) : BigDecimal.ZERO;
            BigDecimal neg = negativeRow != null && negativeRow.getPeriodValues() != null ? negativeRow.getPeriodValues().getOrDefault(p, BigDecimal.ZERO) : BigDecimal.ZERO;
            BigDecimal diff = pos.subtract(neg);
            diffPeriods.put(p, diff);
            total = total.add(diff);
        }

        return PnlReportRowDTO.builder()
                .name(name)
                .isCalculated(true)
                .isHeader(false)
                .isSubtotal(true)
                .isPercentage(false)
                .periodValues(diffPeriods)
                .totalValue(total)
                .build();
    }

    private PnlReportRowDTO calculatePercentageRow(String name, PnlReportRowDTO numeratorRow, PnlReportRowDTO denominatorRow, List<String> periods) {
        Map<String, BigDecimal> pctPeriods = new HashMap<>();
        BigDecimal totalNum = numeratorRow != null && numeratorRow.getTotalValue() != null ? numeratorRow.getTotalValue() : BigDecimal.ZERO;
        BigDecimal totalDen = denominatorRow != null && denominatorRow.getTotalValue() != null ? denominatorRow.getTotalValue() : BigDecimal.ZERO;

        for (String p : periods) {
            BigDecimal num = numeratorRow != null && numeratorRow.getPeriodValues() != null ? numeratorRow.getPeriodValues().getOrDefault(p, BigDecimal.ZERO) : BigDecimal.ZERO;
            BigDecimal den = denominatorRow != null && denominatorRow.getPeriodValues() != null ? denominatorRow.getPeriodValues().getOrDefault(p, BigDecimal.ZERO) : BigDecimal.ZERO;

            if (den.compareTo(BigDecimal.ZERO) == 0) {
                pctPeriods.put(p, BigDecimal.ZERO);
            } else {
                BigDecimal pct = num.divide(den, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                pctPeriods.put(p, pct.setScale(2, RoundingMode.HALF_UP));
            }
        }

        BigDecimal totalPct = BigDecimal.ZERO;
        if (totalDen.compareTo(BigDecimal.ZERO) != 0) {
            totalPct = totalNum.divide(totalDen, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        return PnlReportRowDTO.builder()
                .name(name)
                .isCalculated(true)
                .isHeader(false)
                .isSubtotal(false)
                .isPercentage(true)
                .periodValues(pctPeriods)
                .totalValue(totalPct)
                .build();
    }
}
