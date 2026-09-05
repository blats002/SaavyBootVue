package org.saavy.controllers;

import org.saavy.entity.Product;
import org.saavy.entity.ProductDTO;
import org.saavy.services.JPAService;
import org.saavy.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class ProductController extends BaseController<Product, ProductDTO, Long> {

    @Autowired
    private ProductService productService;

    @Override
    protected JPAService<Product, ProductDTO, Long> getService() {
        return productService;
    }
}
