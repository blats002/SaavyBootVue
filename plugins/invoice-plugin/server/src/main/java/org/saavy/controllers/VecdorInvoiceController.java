package org.saavy.controllers;

import org.saavy.entity.CustomerInvoice;
import org.saavy.entity.CustomerInvoiceDTO;
import org.saavy.entity.VendorInvoice;
import org.saavy.entity.VendorInvoiceDTO;
import org.saavy.services.CustomerInvoiceService;
import org.saavy.services.JPAService;
import org.saavy.services.VendorInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendor-invoices")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class VecdorInvoiceController extends BaseController<VendorInvoice, VendorInvoiceDTO, Long> {

    @Autowired
    private VendorInvoiceService vendorInvoiceService;

    @Override
    protected JPAService<VendorInvoice, VendorInvoiceDTO, Long> getService() {
        return vendorInvoiceService;
    }
}
