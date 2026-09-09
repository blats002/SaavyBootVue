package org.saavy.config;

import org.saavy.component.EntityRegistryProvider;
import org.saavy.entity.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InvoiceEntityRegistryProvider implements EntityRegistryProvider {

    @Override
    public Map<String, Class<?>> getEntities() {
        return Map.ofEntries(
                Map.entry("parties", PartyDTO.class),
                Map.entry("invoices", InvoiceDTO.class),
                Map.entry("payments", PaymentDTO.class),
                Map.entry("invoice-files", InvoiceFileDTO.class),
                Map.entry("customer-invoices", CustomerInvoiceDTO.class),
                Map.entry("customer-invoice-files", CustomerInvoiceFileDTO.class),
                Map.entry("customer-payments", CustomerPaymentDTO.class),
                Map.entry("vendor-invoices", VendorInvoiceDTO.class),
                Map.entry("vendor-invoice-files", VendorInvoiceFileDTO.class),
                Map.entry("vendor-payments", VendorPaymentDTO.class)
        );
    }
}
