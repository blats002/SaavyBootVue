package org.saavy.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRuleRepository extends JpaRepository<VendorRule, Long> {
    List<VendorRule> findByPatternContainingIgnoreCase(String pattern);
}
