package org.saavy.controllers;

import org.saavy.entity.InvoiceFile;
import org.saavy.entity.InvoiceFileDTO;
import org.saavy.services.InvoiceFileService;
import org.saavy.services.JPAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoice-files")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class InvoiceFileController extends BaseController<InvoiceFile, InvoiceFileDTO, Long> {

    @Autowired
    private InvoiceFileService invoiceFileService;

    @Override
    protected JPAService<InvoiceFile, InvoiceFileDTO, Long> getService() {
        return invoiceFileService;
    }
}
