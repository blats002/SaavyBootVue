package org.saavy.entity;

import org.saavy.reference.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeBadgeRepository extends BaseJpaRepository<EmployeeBadge, Long> {
    Optional<EmployeeBadge> findByQrTokenAndIsActiveTrue(String qrToken);
    Optional<EmployeeBadge> findByPinCodeAndIsActiveTrue(String pinCode);
    Optional<EmployeeBadge> findByEmployeeId(String employeeId);
    long countByIsActiveTrue();
}
