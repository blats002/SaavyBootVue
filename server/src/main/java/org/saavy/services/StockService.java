package org.saavy.services;

import org.saavy.entity.ProductRepository;
import org.saavy.entity.Stock;
import org.saavy.entity.StockRepository;
import org.saavy.entity.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StockService implements JPAService<Stock, Long> {
    
    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Stock> findAll() {
        return stockRepository.findAll();
    }

    @Override
    public Optional<Stock> findById(Long aLong) {
        return stockRepository.findById(aLong);
    }

    @Override
    public Stock save(Stock entity) {
        return stockRepository.save(entity);
    }

    @Override
    public Stock update(Long aLong, Stock entity) {
        entity.setId(aLong);
        return stockRepository.save(entity);
    }

    public List<Stock> findByParentId(String field, Long id) {
        if(field.equals("product")){
            return stockRepository.findAllByProduct(productRepository.findById(id));
        }
        return JPAService.super.findByParentId(field, id);
    }

    @Override
    public void deleteById(Long aLong) {
        stockRepository.deleteById(aLong);
    }
}
