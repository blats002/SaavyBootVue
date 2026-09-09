package org.saavy.config;

import org.saavy.component.DashboardTableProvider;
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
public class InvoiceDashboardTableProvider implements DashboardTableProvider {

    @Autowired
    private CustomerInvoiceRepository invoiceRepository;

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
    public List<Map<String, Object>> getTables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate next7Days = today.plusDays(7);

        // 1. Monthly Profit & Loss Summary Table (Accrual Basis - Full 12 Months)
        try {
            List<CustomerInvoice> customerInvoices = invoiceRepository.findAll();
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
            // All 12 months (descending: Dec to Jan)
            List<YearMonth> months = new ArrayList<>();
            for (int m = 12; m >= 1; m--) {
                months.add(YearMonth.of(targetYear, m));
            }

            List<Map<String, Object>> pnlRows = new ArrayList<>();
            for (YearMonth ym : months) {
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

                String status;
                if (netProfit.compareTo(BigDecimal.ZERO) > 0) {
                    status = "PROFITABLE";
                } else if (netProfit.compareTo(BigDecimal.ZERO) < 0) {
                    status = "LOSS";
                } else {
                    status = "BREAKEVEN";
                }

                Map<String, Object> row = new HashMap<>();
                row.put("month", ym.format(MONTH_FORMATTER));
                row.put("income", monthIncome);
                row.put("expenses", monthExpenses);
                row.put("netProfit", netProfit);
                row.put("status", status);
                pnlRows.add(row);
            }

            List<Map<String, Object>> pnlColumns = List.of(
                    Map.of("field", "month", "header", "Month", "sortable", true),
                    Map.of("field", "income", "header", "Invoiced", "type", "currency", "sortable", true),
                    Map.of("field", "expenses", "header", "Billed", "type", "currency", "sortable", true),
                    Map.of("field", "netProfit", "header", "Net Profit", "type", "currency", "sortable", true),
                    Map.of("field", "status", "header", "Performance", "type", "badge", "sortable", true,
                            "severityMap", Map.of("PROFITABLE", "success", "LOSS", "danger", "BREAKEVEN", "info"))
            );

            Map<String, Object> pnlTable = new HashMap<>();
            pnlTable.put("id", "monthly-pnl-table");
            pnlTable.put("title", "Annual Profit & Loss Summary (" + targetYear + " Jan - Dec)");
            pnlTable.put("colSpan", "col-12 xl:col-6");
            pnlTable.put("paginator", true);
            pnlTable.put("rows", 6);
            pnlTable.put("columns", pnlColumns);
            pnlTable.put("data", pnlRows);
            pnlTable.put("action", Map.of(
                    "label", "View Invoices",
                    "icon", "pi pi-arrow-right",
                    "to", "/pages/customer-invoices"
            ));

            tables.add(pnlTable);
        } catch (Exception e) {
            // In case tables not ready
        }

        // 2. Monthly Cash Flow Summary Table (Cash Basis - Full 12 Months)
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
            for (int m = 12; m >= 1; m--) {
                months.add(YearMonth.of(targetYear, m));
            }

            List<Map<String, Object>> cashFlowRows = new ArrayList<>();
            for (YearMonth ym : months) {
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

                String status;
                if (netCashFlow.compareTo(BigDecimal.ZERO) > 0) {
                    status = "NET POSITIVE";
                } else if (netCashFlow.compareTo(BigDecimal.ZERO) < 0) {
                    status = "NET NEGATIVE";
                } else {
                    status = "NEUTRAL";
                }

                Map<String, Object> row = new HashMap<>();
                row.put("month", ym.format(MONTH_FORMATTER));
                row.put("cashIn", cashIn);
                row.put("cashOut", cashOut);
                row.put("netCashFlow", netCashFlow);
                row.put("status", status);
                cashFlowRows.add(row);
            }

            List<Map<String, Object>> cashFlowColumns = List.of(
                    Map.of("field", "month", "header", "Month", "sortable", true),
                    Map.of("field", "cashIn", "header", "Cash In", "type", "currency", "sortable", true),
                    Map.of("field", "cashOut", "header", "Cash Out", "type", "currency", "sortable", true),
                    Map.of("field", "netCashFlow", "header", "Net Cash Flow", "type", "currency", "sortable", true),
                    Map.of("field", "status", "header", "Flow Status", "type", "badge", "sortable", true,
                            "severityMap", Map.of("NET POSITIVE", "success", "NET NEGATIVE", "danger", "NEUTRAL", "info"))
            );

            Map<String, Object> cashFlowTable = new HashMap<>();
            cashFlowTable.put("id", "monthly-cashflow-table");
            cashFlowTable.put("title", "Annual Cash Flow Summary (" + targetYear + " Jan - Dec)");
            cashFlowTable.put("colSpan", "col-12 xl:col-6");
            cashFlowTable.put("paginator", true);
            cashFlowTable.put("rows", 6);
            cashFlowTable.put("columns", cashFlowColumns);
            cashFlowTable.put("data", cashFlowRows);
            cashFlowTable.put("action", Map.of(
                    "label", "Manage Bills",
                    "icon", "pi pi-arrow-right",
                    "to", "/pages/vendor-invoices"
            ));

            tables.add(cashFlowTable);
        } catch (Exception e) {
            // In case tables not ready
        }

        // 3. Bills Due in Next 7 Days & Overdue
        try {
            List<VendorInvoice> allVendorInvoices = vendorInvoiceRepository.findAll();

            List<Map<String, Object>> billRows = allVendorInvoices.stream()
                    .filter(b -> b.getInvoiceStatus() != InvoiceStatus.PAID && b.getInvoiceStatus() != InvoiceStatus.CANCELLED)
                    .filter(b -> b.getDueDate() != null && !b.getDueDate().isAfter(next7Days))
                    .sorted(Comparator.comparing(VendorInvoice::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(b -> {
                        Map<String, Object> row = new HashMap<>();
                        boolean isOverdue = b.getDueDate() != null && today.isAfter(b.getDueDate());
                        row.put("id", b.getId());
                        row.put("invoiceNumber", b.getInvoiceNumber());
                        row.put("partyName", b.getParty() != null ? b.getParty().getName() : "N/A");
                        row.put("dueDate", b.getDueDate() != null ? b.getDueDate().toString() : null);
                        row.put("totalAmount", b.getTotalAmount());
                        row.put("outstandingAmount", b.getOutstandingAmount());
                        row.put("invoiceStatus", isOverdue ? "OVERDUE" : (b.getInvoiceStatus() != null ? b.getInvoiceStatus().name() : "PENDING"));
                        return row;
                    }).collect(Collectors.toList());

            List<Map<String, Object>> billColumns = List.of(
                    Map.of("field", "invoiceNumber", "header", "Bill #", "sortable", true),
                    Map.of("field", "partyName", "header", "Vendor", "sortable", true),
                    Map.of("field", "dueDate", "header", "Due Date", "type", "date", "sortable", true),
                    Map.of("field", "totalAmount", "header", "Total", "type", "currency", "sortable", true),
                    Map.of("field", "outstandingAmount", "header", "Amount Due", "type", "currency", "sortable", true),
                    Map.of("field", "invoiceStatus", "header", "Status", "type", "badge", "sortable", true)
            );

            Map<String, Object> billsTable = new HashMap<>();
            billsTable.put("id", "bills-due-soon-table");
            billsTable.put("title", "Bills Due in Next 7 Days / Overdue");
            billsTable.put("colSpan", "col-12 xl:col-6");
            billsTable.put("paginator", true);
            billsTable.put("rows", 5);
            billsTable.put("columns", billColumns);
            billsTable.put("data", billRows);
            billsTable.put("action", Map.of(
                    "label", "Manage Bills",
                    "icon", "pi pi-arrow-right",
                    "to", "/pages/vendor-invoices"
            ));

            tables.add(billsTable);
        } catch (Exception e) {
            // In case tables not ready
        }

        // 4. Recent Invoices Overview
        try {
            List<CustomerInvoice> invoices = invoiceRepository.findAll();

            invoices.sort((a, b) -> Long.compare(
                    b.getId() != null ? b.getId() : 0L,
                    a.getId() != null ? a.getId() : 0L
            ));

            List<Map<String, Object>> rows = invoices.stream().limit(10).map(i -> {
                Map<String, Object> row = new HashMap<>();
                boolean isOverdue = i.getDueDate() != null && today.isAfter(i.getDueDate()) && i.getInvoiceStatus() != InvoiceStatus.PAID && i.getInvoiceStatus() != InvoiceStatus.CANCELLED;
                row.put("id", i.getId());
                row.put("invoiceNumber", i.getInvoiceNumber());
                row.put("partyName", i.getParty() != null ? i.getParty().getName() : "N/A");
                row.put("invoiceDate", i.getInvoiceDate() != null ? i.getInvoiceDate().toString() : null);
                row.put("dueDate", i.getDueDate() != null ? i.getDueDate().toString() : null);
                row.put("totalAmount", i.getTotalAmount());
                row.put("outstandingAmount", i.getOutstandingAmount());
                row.put("invoiceStatus", isOverdue ? "OVERDUE" : (i.getInvoiceStatus() != null ? i.getInvoiceStatus().name() : ""));
                return row;
            }).collect(Collectors.toList());

            List<Map<String, Object>> columns = List.of(
                    Map.of("field", "invoiceNumber", "header", "Invoice #", "sortable", true),
                    Map.of("field", "partyName", "header", "Customer", "sortable", true),
                    Map.of("field", "dueDate", "header", "Due Date", "type", "date", "sortable", true),
                    Map.of("field", "totalAmount", "header", "Total", "type", "currency", "sortable", true),
                    Map.of("field", "outstandingAmount", "header", "Outstanding", "type", "currency", "sortable", true),
                    Map.of("field", "invoiceStatus", "header", "Status", "type", "badge", "sortable", true)
            );

            Map<String, Object> table = new HashMap<>();
            table.put("id", "recent-invoices-table");
            table.put("title", "Recent Invoices");
            table.put("colSpan", "col-12 xl:col-6");
            table.put("paginator", true);
            table.put("rows", 5);
            table.put("columns", columns);
            table.put("data", rows);
            table.put("action", Map.of(
                    "label", "View Invoices",
                    "icon", "pi pi-arrow-right",
                    "to", "/pages/customer-invoices"
            ));

            tables.add(table);
        } catch (Exception e) {
            // In case tables not ready
        }

        return tables;
    }
}
