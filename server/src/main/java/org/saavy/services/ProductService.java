package org.saavy.services;

import org.saavy.entity.Product;
import org.saavy.entity.ProductDTO;
import org.saavy.entity.ProductRepository;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends JPAService<Product, ProductDTO, Long> {

    @Autowired
    private ProductRepository productRepository;

    @Override
    protected BaseJpaRepository<Product, Long> getJpaRepository() {
        return productRepository;
    }

    @Override
    public ProductDTO toDTO(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        return dto;
    }

    @Override
    public Product toEntity(ProductDTO dto, Long id) {
        Product product = new Product();
        product.setId(id != null ? id : dto.getId());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        return product;
    }
}
