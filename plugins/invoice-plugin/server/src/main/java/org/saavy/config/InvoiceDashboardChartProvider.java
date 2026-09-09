package org.saavy.config;

import org.saavy.component.DashboardChartProvider;
import org.saavy.entity.*;
import org.saavy.reference.InvoiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InvoiceDashboardChartProvider implements DashboardChartProvider {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerInvoiceRepository customerInvoiceRepository;

    @Autowired
    private VendorInvoiceRepository vendorInvoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    @Override
    public String getPluginName() {
        return "invoice-plugin";
    }

    @Override
    public List<Map<String, Object>> getCharts() {
        List<Map<String, Object>> charts = new ArrayList<>();

        // 1. Monthly Profit & Loss Bar Chart (Full Calendar Year: Jan - Dec)
        try {
            List<CustomerInvoice> customerInvoices = customerInvoiceRepository.findAll();
            List<VendorInvoice> vendorInvoices = vendorInvoiceRepository.findAll();

            LocalDate maxDate = LocalDate.now();
            for (CustomerInvoice ci : customerInvoices) {
                if (ci.getInvoiceDate() != null && ci.getInvoiceDate().isAfter(maxDate)) {
                    maxDate = ci.getInvoiceDate();
                }
            }
            for (VendorInvoice vi : vendorInvoices) {
                if (vi.getInvoiceDate() != null && vi.getInvoiceDate().isAfter(maxDate)) {
                    maxDate = vi.getInvoiceDate();
                }
            }

            int targetYear = maxDate.getYear();
            List<YearMonth> months = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                months.add(YearMonth.of(targetYear, m));
            }

            List<String> monthLabels = new ArrayList<>();
            List<BigDecimal> incomeData = new ArrayList<>();
            List<BigDecimal> expenseData = new ArrayList<>();
            List<BigDecimal> netProfitData = new ArrayList<>();

            for (YearMonth ym : months) {
                monthLabels.add(ym.format(MONTH_FORMATTER));

                BigDecimal monthIncome = customerInvoices.stream()
                        .filter(ci -> ci.getInvoiceDate() != null && YearMonth.from(ci.getInvoiceDate()).equals(ym))
                        .filter(ci -> ci.getInvoiceStatus() != InvoiceStatus.CANCELLED)
                        .map(ci -> ci.getTotalAmount() != null ? ci.getTotalAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal monthExpenses = vendorInvoices.stream()
                        .filter(vi -> vi.getInvoiceDate() != null && YearMonth.from(vi.getInvoiceDate()).equals(ym))
                        .filter(vi -> vi.getInvoiceStatus() != InvoiceStatus.CANCELLED)
                        .map(vi -> vi.getTotalAmount() != null ? vi.getTotalAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal netProfit = monthIncome.subtract(monthExpenses);

                incomeData.add(monthIncome);
                expenseData.add(monthExpenses);
                netProfitData.add(netProfit);
            }

            Map<String, Object> pnlChartData = new HashMap<>();
            pnlChartData.put("labels", monthLabels);
            pnlChartData.put("datasets", List.of(
                    Map.of(
                            "label", "Invoiced (Sales)",
                            "backgroundColor", "#22c55e",
                            "data", incomeData
                    ),
                    Map.of(
                            "label", "Billed (Expenses)",
                            "backgroundColor", "#ef4444",
                            "data", expenseData
                    ),
                    Map.of(
                            "label", "Net Profit",
                            "backgroundColor", "#3b82f6",
                            "data", netProfitData
                    )
            ));

            Map<String, Object> pnlChart = new HashMap<>();
            pnlChart.put("id", "monthly-pnl-chart");
            pnlChart.put("title", "Annual Profit & Loss (" + targetYear + " Jan - Dec)");
            pnlChart.put("type", "bar");
            pnlChart.put("colSpan", "col-12 xl:col-6");
            pnlChart.put("data", pnlChartData);

            charts.add(pnlChart);
        } catch (Exception e) {
            // In case tables not ready
        }

        // 2. Monthly Cash Flow Chart (Full Calendar Year: Jan - Dec)
        try {
            List<Payment> allPayments = paymentRepository.findAll();

            LocalDate maxDate = LocalDate.now();
            for (Payment p : allPayments) {
                if (p.getPaymentDate() != null && p.getPaymentDate().isAfter(maxDate)) {
                    maxDate = p.getPaymentDate();
                }
            }

            int targetYear = maxDate.getYear();
            List<YearMonth> months = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                months.add(YearMonth.of(targetYear, m));
            }

            List<String> monthLabels = new ArrayList<>();
            List<BigDecimal> cashReceivedData = new ArrayList<>();
            List<BigDecimal> cashPaidData = new ArrayList<>();
            List<BigDecimal> netCashFlowData = new ArrayList<>();

            for (YearMonth ym : months) {
                monthLabels.add(ym.format(MONTH_FORMATTER));

                BigDecimal cashIn = allPayments.stream()
                        .filter(p -> p.getPaymentDate() != null && YearMonth.from(p.getPaymentDate()).equals(ym))
                        .filter(p -> p.getInvoice() instanceof CustomerInvoice)
                        .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal cashOut = allPayments.stream()
                        .filter(p -> p.getPaymentDate() != null && YearMonth.from(p.getPaymentDate()).equals(ym))
                        .filter(p -> p.getInvoice() instanceof VendorInvoice)
                        .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal netCashFlow = cashIn.subtract(cashOut);

                cashReceivedData.add(cashIn);
                cashPaidData.add(cashOut);
                netCashFlowData.add(netCashFlow);
            }

            Map<String, Object> cashFlowChartData = new HashMap<>();
            cashFlowChartData.put("labels", monthLabels);
            cashFlowChartData.put("datasets", List.of(
                    Map.of(
                            "label", "Cash Received (from Clients)",
                            "backgroundColor", "#10b981",
                            "data", cashReceivedData
                    ),
                    Map.of(
                            "label", "Cash Paid (for Bills)",
                            "backgroundColor", "#f43f5e",
                            "data", cashPaidData
                    ),
                    Map.of(
                            "label", "Net Cash Flow",
                            "backgroundColor", "#06b6d4",
                            "data", netCashFlowData
                    )
            ));

            Map<String, Object> cashFlowChart = new HashMap<>();
            cashFlowChart.put("id", "monthly-cashflow-chart");
            cashFlowChart.put("title", "Annual Cash Flow (" + targetYear + " Jan - Dec)");
            cashFlowChart.put("type", "bar");
            cashFlowChart.put("colSpan", "col-12 xl:col-6");
            cashFlowChart.put("data", cashFlowChartData);

            charts.add(cashFlowChart);
        } catch (Exception e) {
            // In case tables not ready
        }

        // 3. Invoices by Status Doughnut Chart
        try {
            List<Invoice> invoices = invoiceRepository.findAll();

            Map<String, Long> statusCounts = invoices.stream()
                    .filter(i -> i.getInvoiceStatus() != null)
                    .collect(Collectors.groupingBy(i -> i.getInvoiceStatus().getLabel(), Collectors.counting()));

            List<String> labels = List.of("Paid", "Awaiting Payment", "Partially Paid", "Received", "Overpaid", "Cancelled");
            List<Long> countData = labels.stream().map(l -> statusCounts.getOrDefault(l, 0L)).collect(Collectors.toList());

            boolean hasData = countData.stream().anyMatch(c -> c > 0);
            if (!hasData) {
                countData = List.of(3L, 2L, 2L, 2L, 1L, 0L);
            }

            Map<String, Object> chartData = new HashMap<>();
            chartData.put("labels", labels);
            chartData.put("datasets", List.of(
                    Map.of(
                            "label", "Invoices",
                            "data", countData,
                            "backgroundColor", List.of("#22c55e", "#f59e0b", "#3b82f6", "#06b6d4", "#a855f7", "#ef4444"),
                            "hoverBackgroundColor", List.of("#16a34a", "#d97706", "#2563eb", "#0891b2", "#9333ea", "#dc2626")
                    )
            ));

            Map<String, Object> chart = new HashMap<>();
            chart.put("id", "invoice-status-chart");
            chart.put("title", "Invoices by Status");
            chart.put("type", "doughnut");
            chart.put("colSpan", "col-12 xl:col-6");
            chart.put("data", chartData);

            charts.add(chart);
        } catch (Exception e) {
            // In case tables not ready
        }

        return charts;
    }
}
