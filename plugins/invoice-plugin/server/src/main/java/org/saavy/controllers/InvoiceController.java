package org.saavy.controllers;

import org.saavy.entity.Invoice;
import org.saavy.entity.InvoiceDTO;
import org.saavy.services.InvoiceService;
import org.saavy.services.JPAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class InvoiceController extends BaseController<Invoice, InvoiceDTO, Long> {

    @Autowired
    private InvoiceService invoiceService;

    @Override
    protected JPAService<Invoice, InvoiceDTO, Long> getService() {
        return invoiceService;
    }
}
