package org.saavy.services;

import org.saavy.entity.Supplier;
import org.saavy.entity.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService implements JPAService<Supplier, Long> {

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    @Override
    public Optional<Supplier> findById(Long aLong) {
        return supplierRepository.findById(aLong);
    }

    @Override
    public Supplier save(Supplier entity) {
        return supplierRepository.save(entity);
    }

    @Override
    public Supplier update(Long aLong, Supplier entity) {
        entity.setId(aLong);
        return supplierRepository.save(entity);
    }

    @Override
    public void deleteById(Long aLong) {
        supplierRepository.deleteById(aLong);
    }
}
