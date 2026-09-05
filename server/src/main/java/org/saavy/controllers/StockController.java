package org.saavy.controllers;

import org.saavy.entity.Stock;
import org.saavy.entity.StockDTO;
import org.saavy.services.JPAService;
import org.saavy.services.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class StockController extends BaseController<Stock, StockDTO, Long> {

    @Autowired
    private StockService stockService;

    @Override
    protected JPAService<Stock, StockDTO, Long> getService() {
        return stockService;
    }
}
