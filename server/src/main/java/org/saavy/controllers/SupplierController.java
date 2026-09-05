package org.saavy.controllers;

import org.saavy.entity.Supplier;
import org.saavy.entity.SupplierDTO;
import org.saavy.services.JPAService;
import org.saavy.services.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class SupplierController extends BaseController<Supplier, SupplierDTO, Long> {

    @Autowired
    private SupplierService supplierService;

    @Override
    protected JPAService<Supplier, SupplierDTO, Long> getService() {
        return supplierService;
    }
}
