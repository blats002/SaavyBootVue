package org.saavy.controllers;

import org.saavy.entity.CustomerInvoice;
import org.saavy.entity.CustomerInvoiceDTO;
import org.saavy.entity.Invoice;
import org.saavy.entity.InvoiceDTO;
import org.saavy.services.CustomerInvoiceService;
import org.saavy.services.JPAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-invoices")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class CustomerInvoiceController extends BaseController<CustomerInvoice, CustomerInvoiceDTO, Long> {

    @Autowired
    private CustomerInvoiceService customerInvoiceService;

    @Override
    protected JPAService<CustomerInvoice, CustomerInvoiceDTO, Long> getService() {
        return customerInvoiceService;
    }
}
