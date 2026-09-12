package org.saavy.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxCategoryRepository extends JpaRepository<TaxCategory, Long> {
    Optional<TaxCategory> findByCode(String code);
    List<TaxCategory> findByPnlCategory(String pnlCategory);
    List<TaxCategory> findByIsDeductible(Boolean isDeductible);
}
