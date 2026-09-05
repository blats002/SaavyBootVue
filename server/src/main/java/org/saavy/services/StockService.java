package org.saavy.services;

import org.saavy.entity.*;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockService extends JPAService<Stock, StockDTO, Long> {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private SupplierService supplierService;

    @Override
    protected BaseJpaRepository<Stock, Long> getJpaRepository() {
        return stockRepository;
    }

    @Override
    public StockDTO toDTO(Stock entity) {
        return new StockDTO(
                entity.getId(),
                productService.toDTO(entity.getProduct()),
                supplierService.toDTO(entity.getSupplier()),
                entity.getQuantity(),
                entity.getDisplayValue()
        );
    }

    @Override
    public Stock toEntity(StockDTO dto, Long id) {
        Stock stock = new Stock();
        stock.setId(id != null ? id : dto.getId());
        stock.setProduct(productRepository.getReferenceById(dto.getProduct().getId()));
        stock.setSupplier(supplierRepository.getReferenceById(dto.getSupplier().getId()));
        stock.setQuantity(dto.getQuantity());
        return stock;
    }
}
