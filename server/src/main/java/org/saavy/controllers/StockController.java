package org.saavy.controllers;

import org.saavy.entity.Stock;
import org.saavy.entity.Supplier;
import org.saavy.services.JPAService;
import org.saavy.services.StockService;
import org.saavy.services.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController extends BaseController<Stock, Long>{

    @Autowired
    private StockService stockService;

    @Override
    protected JPAService<Stock, Long> getService() {
        return stockService;
    }
}
