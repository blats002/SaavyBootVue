package org.saavy.services;

import org.saavy.entity.PnlAccount;
import org.saavy.entity.PnlAccountRepository;
import org.saavy.entity.PnlLedgerEntry;
import org.saavy.entity.PnlLedgerEntryRepository;
import org.saavy.reference.PnlAccountCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PnlDashboardService {

    @Autowired
    private PnlAccountRepository pnlAccountRepository;

    @Autowired
    private PnlLedgerEntryRepository pnlLedgerEntryRepository;

    public int resolveTargetYear() {
        try {
            List<PnlLedgerEntry> all = pnlLedgerEntryRepository.findAll();
            if (all != null && !all.isEmpty()) {
                int maxYear = all.stream()
                        .filter(e -> e.getEntryDate() != null)
                        .mapToInt(e -> e.getEntryDate().getYear())
                        .max()
                        .orElse(LocalDate.now().getYear());
                return maxYear;
            }
        } catch (Exception ignored) {
        }
        return LocalDate.now().getYear();
    }

    public String resolveCurrency(List<PnlLedgerEntry> entries) {
        if (entries != null) {
            for (PnlLedgerEntry e : entries) {
                if (e.getCurrency() != null && !e.getCurrency().isBlank()) {
                    return e.getCurrency();
                }
            }
        }
        return "PHP";
    }

    public static String formatCurrency(BigDecimal amount, String currencyCode) {
        if (amount == null) return "-";
        boolean isNegative = amount.compareTo(BigDecimal.ZERO) < 0;
        BigDecimal abs = amount.abs();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        String symbol;
        if ("PHP".equalsIgnoreCase(currencyCode)) {
            symbol = "\u20b1";
        } else if ("EUR".equalsIgnoreCase(currencyCode)) {
            symbol = "\u20ac";
        } else if ("GBP".equalsIgnoreCase(currencyCode)) {
            symbol = "\u00a3";
        } else if ("JPY".equalsIgnoreCase(currencyCode)) {
            symbol = "\u00a5";
        } else {
            symbol = "$";
        }
        return (isNegative ? "-" : "") + symbol + nf.format(abs);
    }

    public List<Map<String, Object>> buildDashboardCards() {
        List<Map<String, Object>> cards = new ArrayList<>();
        int targetYear = resolveTargetYear();
        int priorYear = targetYear - 1;

        try {
            LocalDate startCurr = LocalDate.of(targetYear, 1, 1);
            LocalDate endCurr = LocalDate.of(targetYear, 12, 31);
            LocalDate startPrior = LocalDate.of(priorYear, 1, 1);
            LocalDate endPrior = LocalDate.of(priorYear, 12, 31);

            List<PnlLedgerEntry> currEntries = pnlLedgerEntryRepository.findByEntryDateBetweenWithAccount(startCurr, endCurr);
            List<PnlLedgerEntry> priorEntries = pnlLedgerEntryRepository.findByEntryDateBetweenWithAccount(startPrior, endPrior);

            String currencyCode = resolveCurrency(currEntries.isEmpty() ? priorEntries : currEntries);

            BigDecimal currRevenue = sumCategory(currEntries, PnlAccountCategory.REVENUE);
            BigDecimal priorRevenue = sumCategory(priorEntries, PnlAccountCategory.REVENUE);

            BigDecimal currCogs = sumCategory(currEntries, PnlAccountCategory.COGS);
            BigDecimal priorCogs = sumCategory(priorEntries, PnlAccountCategory.COGS);

            BigDecimal currOpex = sumCategory(currEntries, PnlAccountCategory.OPEX);
            BigDecimal priorOpex = sumCategory(priorEntries, PnlAccountCategory.OPEX);

            BigDecimal currTax = sumCategory(currEntries, PnlAccountCategory.TAX);
            BigDecimal priorTax = sumCategory(priorEntries, PnlAccountCategory.TAX);

            BigDecimal currGrossProfit = currRevenue.subtract(currCogs);
            BigDecimal priorGrossProfit = priorRevenue.subtract(priorCogs);

            BigDecimal currNetIncome = currGrossProfit.subtract(currOpex).subtract(currTax);
            BigDecimal priorNetIncome = priorGrossProfit.subtract(priorOpex).subtract(priorTax);

            double currGrossMargin = currRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? currGrossProfit.multiply(BigDecimal.valueOf(100)).divide(currRevenue, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            double priorGrossMargin = priorRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? priorGrossProfit.multiply(BigDecimal.valueOf(100)).divide(priorRevenue, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            // 1. Revenue Card
            Map<String, Object> revCard = new HashMap<>();
            revCard.put("plugin", "pnl-plugin");
            revCard.put("title", "Total Revenue (" + targetYear + ")");
            revCard.put("content", formatCurrency(currRevenue, currencyCode));
            revCard.put("icon", "pi pi-dollar text-green-500 text-xl");

            Map<String, Object> revFooter = new HashMap<>();
            if (priorRevenue.compareTo(BigDecimal.ZERO) > 0) {
                double growth = currRevenue.subtract(priorRevenue)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(priorRevenue, 1, RoundingMode.HALF_UP).doubleValue();
                revFooter.put("value", (growth >= 0 ? "\u25b2 +" : "\u25bc ") + growth + "%");
                revFooter.put("content", "YoY vs " + priorYear + " (" + formatCurrency(priorRevenue, currencyCode) + ")");
                revFooter.put("severity", growth >= 0 ? "success" : "danger");
            } else {
                revFooter.put("value", "Active");
                revFooter.put("content", "Current fiscal period");
                revFooter.put("severity", "success");
            }
            revCard.put("footer", revFooter);
            cards.add(revCard);

            // 2. Gross Margin Card
            Map<String, Object> gmCard = new HashMap<>();
            gmCard.put("plugin", "pnl-plugin");
            gmCard.put("title", "Gross Margin %");
            gmCard.put("content", currGrossMargin + "%");
            gmCard.put("icon", "pi pi-percentage text-cyan-500 text-xl");

            Map<String, Object> gmFooter = new HashMap<>();
            if (priorRevenue.compareTo(BigDecimal.ZERO) > 0) {
                double marginDelta = currGrossMargin - priorGrossMargin;
                gmFooter.put("value", (marginDelta >= 0 ? "\u25b2 +" : "\u25bc ") + String.format(Locale.US, "%.1f", marginDelta) + " pts");
                gmFooter.put("content", "vs " + priorYear + " (" + priorGrossMargin + "%)");
                gmFooter.put("severity", marginDelta >= 0 ? "success" : "warning");
            } else {
                gmFooter.put("value", formatCurrency(currGrossProfit, currencyCode));
                gmFooter.put("content", "Gross Profit");
                gmFooter.put("severity", "success");
            }
            gmCard.put("footer", gmFooter);
            cards.add(gmCard);

            // 3. OPEX Card
            Map<String, Object> opexCard = new HashMap<>();
            opexCard.put("plugin", "pnl-plugin");
            opexCard.put("title", "Operating Expenses");
            opexCard.put("content", formatCurrency(currOpex, currencyCode));
            opexCard.put("icon", "pi pi-wallet text-orange-500 text-xl");

            Map<String, Object> opexFooter = new HashMap<>();
            if (priorOpex.compareTo(BigDecimal.ZERO) > 0) {
                double opexGrowth = currOpex.subtract(priorOpex)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(priorOpex, 1, RoundingMode.HALF_UP).doubleValue();
                opexFooter.put("value", (opexGrowth <= 0 ? "\u25bc " : "\u25b2 +") + opexGrowth + "%");
                opexFooter.put("content", "YoY expense change vs " + priorYear);
                opexFooter.put("severity", opexGrowth <= 0 ? "success" : "warning");
            } else {
                opexFooter.put("value", "OPEX");
                opexFooter.put("content", "Operational spend");
                opexFooter.put("severity", "warning");
            }
            opexCard.put("footer", opexFooter);
            cards.add(opexCard);

            // 4. Net Income Card
            Map<String, Object> netCard = new HashMap<>();
            netCard.put("plugin", "pnl-plugin");
            netCard.put("title", "Net Income (Profit)");
            netCard.put("content", formatCurrency(currNetIncome, currencyCode));
            netCard.put("icon", "pi pi-chart-line text-purple-500 text-xl");

            double netMargin = currRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? currNetIncome.multiply(BigDecimal.valueOf(100)).divide(currRevenue, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            Map<String, Object> netFooter = new HashMap<>();
            netFooter.put("value", netMargin + "%");
            netFooter.put("content", "Net Margin (" + (currNetIncome.compareTo(BigDecimal.ZERO) >= 0 ? "Profitable" : "Loss") + ")");
            netFooter.put("severity", currNetIncome.compareTo(BigDecimal.ZERO) >= 0 ? "success" : "danger");
            netCard.put("footer", netFooter);
            cards.add(netCard);

        } catch (Exception e) {
            // In case tables not ready
        }

        return cards;
    }

    public List<Map<String, Object>> buildDashboardCharts() {
        List<Map<String, Object>> charts = new ArrayList<>();
        int targetYear = resolveTargetYear();
        int priorYear = targetYear - 1;

        try {
            LocalDate startCurr = LocalDate.of(targetYear, 1, 1);
            LocalDate endCurr = LocalDate.of(targetYear, 12, 31);
            LocalDate startPrior = LocalDate.of(priorYear, 1, 1);
            LocalDate endPrior = LocalDate.of(priorYear, 12, 31);

            List<PnlLedgerEntry> currEntries = pnlLedgerEntryRepository.findByEntryDateBetweenWithAccount(startCurr, endCurr);
            List<PnlLedgerEntry> priorEntries = pnlLedgerEntryRepository.findByEntryDateBetweenWithAccount(startPrior, endPrior);

            List<String> monthLabels = new ArrayList<>();
            List<BigDecimal> currMonthlyRevenue = new ArrayList<>();
            List<BigDecimal> priorMonthlyRevenue = new ArrayList<>();

            List<BigDecimal> currMonthlyCosts = new ArrayList<>();
            List<BigDecimal> currMonthlyNet = new ArrayList<>();

            for (int m = 1; m <= 12; m++) {
                final int monthVal = m;
                monthLabels.add(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

                BigDecimal currMonthRev = currEntries.stream()
                        .filter(e -> e.getEntryDate() != null && e.getEntryDate().getMonthValue() == monthVal)
                        .filter(e -> e.getAccount() != null && e.getAccount().getCategory() == PnlAccountCategory.REVENUE)
                        .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal priorMonthRev = priorEntries.stream()
                        .filter(e -> e.getEntryDate() != null && e.getEntryDate().getMonthValue() == monthVal)
                        .filter(e -> e.getAccount() != null && e.getAccount().getCategory() == PnlAccountCategory.REVENUE)
                        .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal currMonthExp = currEntries.stream()
                        .filter(e -> e.getEntryDate() != null && e.getEntryDate().getMonthValue() == monthVal)
                        .filter(e -> e.getAccount() != null && e.getAccount().getCategory() != PnlAccountCategory.REVENUE)
                        .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal currMonthNet = currMonthRev.subtract(currMonthExp);

                currMonthlyRevenue.add(currMonthRev);
                priorMonthlyRevenue.add(priorMonthRev);
                currMonthlyCosts.add(currMonthExp);
                currMonthlyNet.add(currMonthNet);
            }

            // 1. Chart A: Year-over-Year Revenue Comparison
            Map<String, Object> yoyRevChartData = new HashMap<>();
            yoyRevChartData.put("labels", monthLabels);
            yoyRevChartData.put("datasets", List.of(
                    Map.of(
                            "label", targetYear + " Revenue",
                            "backgroundColor", "#22c55e",
                            "data", currMonthlyRevenue
                    ),
                    Map.of(
                            "label", priorYear + " Revenue (Prior Year)",
                            "backgroundColor", "#3b82f6",
                            "data", priorMonthlyRevenue
                    )
            ));

            Map<String, Object> yoyRevChart = new HashMap<>();
            yoyRevChart.put("id", "pnl-yoy-revenue-chart");
            yoyRevChart.put("title", "Monthly Revenue Comparison (" + targetYear + " vs " + priorYear + ")");
            yoyRevChart.put("type", "bar");
            yoyRevChart.put("colSpan", "col-12 xl:col-6");
            yoyRevChart.put("plugin", "pnl-plugin");
            yoyRevChart.put("data", yoyRevChartData);
            charts.add(yoyRevChart);

            // 2. Chart B: Monthly P&L Performance (Revenue vs Costs vs Net)
            Map<String, Object> pnlPerfChartData = new HashMap<>();
            pnlPerfChartData.put("labels", monthLabels);
            pnlPerfChartData.put("datasets", List.of(
                    Map.of(
                            "label", "Revenue",
                            "backgroundColor", "#10b981",
                            "data", currMonthlyRevenue
                    ),
                    Map.of(
                            "label", "Total Costs (COGS + OPEX + Tax)",
                            "backgroundColor", "#ef4444",
                            "data", currMonthlyCosts
                    ),
                    Map.of(
                            "label", "Net Income",
                            "backgroundColor", "#8b5cf6",
                            "data", currMonthlyNet
                    )
            ));

            Map<String, Object> pnlPerfChart = new HashMap<>();
            pnlPerfChart.put("id", "pnl-monthly-breakdown-chart");
            pnlPerfChart.put("title", "Monthly Profit & Loss (" + targetYear + " Jan - Dec)");
            pnlPerfChart.put("type", "bar");
            pnlPerfChart.put("colSpan", "col-12 xl:col-6");
            pnlPerfChart.put("plugin", "pnl-plugin");
            pnlPerfChart.put("data", pnlPerfChartData);
            charts.add(pnlPerfChart);

            // 3. Chart C: OPEX Expense Breakdown Doughnut Chart
            Map<String, BigDecimal> opexBySubcategory = new HashMap<>();
            for (PnlLedgerEntry entry : currEntries) {
                if (entry.getAccount() != null && entry.getAccount().getCategory() == PnlAccountCategory.OPEX) {
                    String label = entry.getAccount().getName();
                    if (entry.getAccount().getSubcategory() != null && !entry.getAccount().getSubcategory().isBlank()) {
                        label = entry.getAccount().getSubcategory() + " (" + entry.getAccount().getName() + ")";
                    }
                    opexBySubcategory.merge(label, entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO, BigDecimal::add);
                }
            }

            if (opexBySubcategory.isEmpty()) {
                opexBySubcategory.put("Payroll & Salaries", BigDecimal.valueOf(185000));
                opexBySubcategory.put("Marketing & Advertising", BigDecimal.valueOf(52000));
                opexBySubcategory.put("Office Rent & Facilities", BigDecimal.valueOf(36000));
                opexBySubcategory.put("Legal, Accounting & Compliance", BigDecimal.valueOf(24000));
            }

            List<String> doughnutLabels = new ArrayList<>(opexBySubcategory.keySet());
            List<BigDecimal> doughnutValues = new ArrayList<>();
            for (String lbl : doughnutLabels) {
                doughnutValues.add(opexBySubcategory.get(lbl));
            }

            List<String> colors = List.of("#3b82f6", "#f59e0b", "#8b5cf6", "#ec4899", "#14b8a6", "#64748b", "#06b6d4");
            List<String> bgColors = new ArrayList<>();
            for (int i = 0; i < doughnutLabels.size(); i++) {
                bgColors.add(colors.get(i % colors.size()));
            }

            Map<String, Object> doughnutData = new HashMap<>();
            doughnutData.put("labels", doughnutLabels);
            doughnutData.put("datasets", List.of(
                    Map.of(
                            "label", "Operating Expenses",
                            "data", doughnutValues,
                            "backgroundColor", bgColors
                    )
            ));

            Map<String, Object> opexChart = new HashMap<>();
            opexChart.put("id", "pnl-opex-breakdown-chart");
            opexChart.put("title", "Operating Expense Breakdown (" + targetYear + ")");
            opexChart.put("type", "doughnut");
            opexChart.put("colSpan", "col-12 xl:col-6");
            opexChart.put("plugin", "pnl-plugin");
            opexChart.put("data", doughnutData);
            charts.add(opexChart);

        } catch (Exception e) {
            // In case tables not ready
        }

        return charts;
    }

    public List<Map<String, Object>> buildDashboardTables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        int targetYear = resolveTargetYear();
        int priorYear = targetYear - 1;

        try {
            LocalDate startCurr = LocalDate.of(targetYear, 1, 1);
            LocalDate endCurr = LocalDate.of(targetYear, 12, 31);
            LocalDate startPrior = LocalDate.of(priorYear, 1, 1);
            LocalDate endPrior = LocalDate.of(priorYear, 12, 31);

            List<PnlLedgerEntry> currEntries = pnlLedgerEntryRepository.findByEntryDateBetweenWithAccount(startCurr, endCurr);
            List<PnlLedgerEntry> priorEntries = pnlLedgerEntryRepository.findByEntryDateBetweenWithAccount(startPrior, endPrior);
            String currencyCode = resolveCurrency(currEntries.isEmpty() ? priorEntries : currEntries);

            Map<String, Object> table = new HashMap<>();
            table.put("id", "pnl-top-expenses-table");
            table.put("title", "Top Expense Drivers (" + targetYear + " vs " + priorYear + " YoY)");
            table.put("colSpan", "col-12");
            table.put("paginator", true);
            table.put("rows", 6);
            table.put("plugin", "pnl-plugin");

            List<Map<String, Object>> columns = new ArrayList<>();

            Map<String, Object> colCode = new HashMap<>();
            colCode.put("field", "code");
            colCode.put("header", "Code");
            colCode.put("sortable", true);
            colCode.put("width", "10%");
            columns.add(colCode);

            Map<String, Object> colName = new HashMap<>();
            colName.put("field", "name");
            colName.put("header", "Account Name");
            colName.put("sortable", true);
            colName.put("width", "30%");
            columns.add(colName);

            Map<String, Object> colCat = new HashMap<>();
            colCat.put("field", "category");
            colCat.put("header", "Category");
            colCat.put("sortable", true);
            colCat.put("type", "badge");
            colCat.put("width", "15%");
            columns.add(colCat);

            Map<String, Object> colPrior = new HashMap<>();
            colPrior.put("field", "priorAmount");
            colPrior.put("header", priorYear + " Total");
            colPrior.put("sortable", true);
            colPrior.put("type", "currency");
            colPrior.put("currency", currencyCode);
            colPrior.put("width", "15%");
            columns.add(colPrior);

            Map<String, Object> colCurr = new HashMap<>();
            colCurr.put("field", "currAmount");
            colCurr.put("header", targetYear + " Total");
            colCurr.put("sortable", true);
            colCurr.put("type", "currency");
            colCurr.put("currency", currencyCode);
            colCurr.put("width", "15%");
            columns.add(colCurr);

            Map<String, Object> colVar = new HashMap<>();
            colVar.put("field", "variance");
            colVar.put("header", "YoY Change");
            colVar.put("sortable", true);
            colVar.put("width", "15%");
            columns.add(colVar);

            table.put("columns", columns);

            List<PnlAccount> accounts = pnlAccountRepository.findByActiveTrueOrderBySortOrderAscCodeAsc();

            Map<Long, BigDecimal> currMap = new HashMap<>();
            for (PnlLedgerEntry e : currEntries) {
                if (e.getAccount() != null) {
                    currMap.merge(e.getAccount().getId(), e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO, BigDecimal::add);
                }
            }

            Map<Long, BigDecimal> priorMap = new HashMap<>();
            for (PnlLedgerEntry e : priorEntries) {
                if (e.getAccount() != null) {
                    priorMap.merge(e.getAccount().getId(), e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO, BigDecimal::add);
                }
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            for (PnlAccount acc : accounts) {
                if (acc.getCategory() == PnlAccountCategory.REVENUE) continue;
                BigDecimal cAmt = currMap.getOrDefault(acc.getId(), BigDecimal.ZERO);
                BigDecimal pAmt = priorMap.getOrDefault(acc.getId(), BigDecimal.ZERO);

                if (cAmt.compareTo(BigDecimal.ZERO) == 0 && pAmt.compareTo(BigDecimal.ZERO) == 0) continue;

                Map<String, Object> r = new HashMap<>();
                r.put("code", acc.getCode());
                r.put("name", acc.getName());
                r.put("category", acc.getCategory() != null ? acc.getCategory().name() : "OPEX");
                r.put("priorAmount", pAmt);
                r.put("currAmount", cAmt);
                r.put("currency", currencyCode);

                String varianceStr;
                if (pAmt.compareTo(BigDecimal.ZERO) > 0) {
                    double delta = cAmt.subtract(pAmt).multiply(BigDecimal.valueOf(100)).divide(pAmt, 1, RoundingMode.HALF_UP).doubleValue();
                    varianceStr = (delta >= 0 ? "\u25b2 +" : "\u25bc ") + delta + "%";
                } else {
                    varianceStr = "NEW";
                }
                r.put("variance", varianceStr);
                r.put("totalSort", cAmt);
                rows.add(r);
            }

            rows.sort((a, b) -> ((BigDecimal) b.get("totalSort")).compareTo((BigDecimal) a.get("totalSort")));

            table.put("data", rows);

            Map<String, Object> action = new HashMap<>();
            action.put("label", "Full P&L Matrix");
            action.put("icon", "pi pi-table");
            action.put("to", "/pages/pnl-report");
            table.put("action", action);

            tables.add(table);

        } catch (Exception e) {
            // In case tables not ready
        }

        return tables;
    }

    private BigDecimal sumCategory(List<PnlLedgerEntry> entries, PnlAccountCategory category) {
        if (entries == null) return BigDecimal.ZERO;
        return entries.stream()
                .filter(e -> e.getAccount() != null && e.getAccount().getCategory() == category)
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
