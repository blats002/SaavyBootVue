package org.saavy.controllers;

import org.saavy.entity.Product;
import org.saavy.services.JPAService;
import org.saavy.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController extends BaseController<Product, Long>{

    @Autowired
    private ProductService productService;

    @Override
    protected JPAService<Product, Long> getService() {
        return productService;
    }

    @Override
    public Product save(@RequestBody Product entity) {
        return getService().save(entity);
    }
}
