package org.saavy.services;

import org.saavy.entity.Product;
import org.saavy.entity.ProductRepository;
import org.saavy.entity.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ProductService implements JPAService<Product, Long> {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findById(Long aLong) {
        return productRepository.findById(aLong);
    }

    @Override
    public Product save(Product entity) {
        return productRepository.save(entity);
    }

    @Override
    public Product update(Long aLong, Product entity) {
        entity.setId(aLong);
        return productRepository.save(entity);
    }

    @Override
    public void deleteById(Long aLong) {
        productRepository.deleteById(aLong);
    }
}
