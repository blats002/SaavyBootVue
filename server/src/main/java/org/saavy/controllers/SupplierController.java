package org.saavy.controllers;

import org.saavy.entity.Supplier;
import org.saavy.services.JPAService;
import org.saavy.services.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController extends BaseController<Supplier, Long>{

    @Autowired
    private SupplierService supplierService;

    @Override
    protected JPAService<Supplier, Long> getService() {
        return supplierService;
    }
}
