package org.saavy.config;

import org.saavy.component.DashboardCardProvider;
import org.saavy.entity.CustomerInvoice;
import org.saavy.entity.CustomerInvoiceRepository;
import org.saavy.entity.VendorInvoice;
import org.saavy.entity.VendorInvoiceRepository;
import org.saavy.reference.InvoiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Component
public class InvoiceDashboardCardProvider implements DashboardCardProvider {

    @Autowired
    private CustomerInvoiceRepository customerInvoiceRepository;

    @Autowired
    private VendorInvoiceRepository vendorInvoiceRepository;

    @Override
    public String getPluginName() {
        return "invoice-plugin";
    }

    @Override
    public List<Map<String, Object>> getCards() {
        List<Map<String, Object>> cards = new ArrayList<>();

        // 1. Accounts Receivable Card
        try {
            List<CustomerInvoice> customerInvoices = customerInvoiceRepository.findAll().stream()
                    .filter(i -> i.getInvoiceStatus() != InvoiceStatus.PAID && i.getInvoiceStatus() != InvoiceStatus.CANCELLED)
                    .toList();

            BigDecimal totalAmount = customerInvoices.stream()
                    .map(CustomerInvoice::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalOutstanding = customerInvoices.stream()
                    .map(CustomerInvoice::getOutstandingAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalPaid = totalAmount.subtract(totalOutstanding);
            double paidPercentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? totalPaid.multiply(BigDecimal.valueOf(100)).divide(totalAmount, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            Map<String, Object> arCard = new HashMap<>();
            arCard.put("title", "Accounts Receivable");
            arCard.put("content", totalAmount.compareTo(BigDecimal.ZERO) == 0
                    ? "No Accounts Receivable"
                    : totalOutstanding + " paid out of " + totalAmount);
            arCard.put("icon", "pi pi-inbox text-cyan-500 text-xl");

            Map<String, Object> arFooter = new HashMap<>();
            arFooter.put("value", (paidPercentage + "%"));
            arFooter.put("content", "total amount of paid clients");
            arFooter.put("severity", paidPercentage > 70 ? "success" : paidPercentage > 30 ? "warning" : "danger");
            arCard.put("footer", arFooter);

            cards.add(arCard);
        } catch (Exception e) {
            // In case table is not populated yet
        }

        // 2. Accounts Payable Card
        try {
            LocalDate today = LocalDate.now();
            LocalDate next7Days = today.plusDays(7);

            List<VendorInvoice> allVendorInvoices = vendorInvoiceRepository.findAll();

            List<VendorInvoice> activeVendorInvoices = allVendorInvoices.stream()
                    .filter(i -> i.getInvoiceStatus() != InvoiceStatus.PAID
                            && i.getInvoiceStatus() != InvoiceStatus.CANCELLED
                            && i.getDueDate() != null && (!i.getDueDate().isAfter(next7Days) || i.getDueDate().isBefore(today)))
                    .toList();

            BigDecimal vendorTotalAmount = activeVendorInvoices.stream()
                    .map(VendorInvoice::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal vendorTotalOutstanding = activeVendorInvoices.stream()
                    .map(VendorInvoice::getOutstandingAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long dueThisWeekCount = activeVendorInvoices.stream()
                    .filter(i -> i.getDueDate() != null && (!i.getDueDate().isAfter(next7Days) || i.getDueDate().isBefore(today)))
                    .count();

            BigDecimal vendorTotalPaid = vendorTotalAmount.subtract(vendorTotalOutstanding);
            double vendorPaidPercentage = vendorTotalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? vendorTotalPaid.multiply(BigDecimal.valueOf(100)).divide(vendorTotalAmount, 2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            Map<String, Object> apCard = new HashMap<>();
            apCard.put("title", "Accounts Payable");
            apCard.put("content", vendorTotalAmount.compareTo(BigDecimal.ZERO) == 0
                    ? "No Accounts Payable"
                    : vendorTotalOutstanding + " outstanding bill out of " + vendorTotalAmount);
            apCard.put("icon", "pi pi-map-marker text-orange-500 text-xl");

            Map<String, Object> apFooter = new HashMap<>();
            apFooter.put("value", dueThisWeekCount == 0 ? "" : String.valueOf(dueThisWeekCount));
            apFooter.put("content", dueThisWeekCount == 0 ? "" : "bills due this week");
            apFooter.put("severity", vendorPaidPercentage == 100.0 ? "success" : "danger");
            apCard.put("footer", apFooter);

            cards.add(apCard);
        } catch (Exception e) {
            // In case table is not populated yet
        }

        return cards;
    }
}
