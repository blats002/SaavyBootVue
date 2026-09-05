package org.saavy.services;

import org.saavy.entity.Supplier;
import org.saavy.entity.SupplierDTO;
import org.saavy.entity.SupplierRepository;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupplierService extends JPAService<Supplier, SupplierDTO, Long> {

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    protected BaseJpaRepository<Supplier, Long> getJpaRepository() {
        return supplierRepository;
    }

    @Override
    public SupplierDTO toDTO(Supplier entity) {
        return new SupplierDTO(
                entity.getId(),
                entity.getName(),
                entity.getContactEmail()
        );
    }

    @Override
    public Supplier toEntity(SupplierDTO dto, Long id) {
        return new Supplier(
                id != null ? id : dto.getId(),
                dto.getName(),
                dto.getContactEmail()
        );
    }
}
