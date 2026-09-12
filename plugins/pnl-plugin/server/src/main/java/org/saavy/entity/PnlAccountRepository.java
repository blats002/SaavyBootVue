package org.saavy.entity;

import org.saavy.reference.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PnlAccountRepository extends BaseJpaRepository<PnlAccount, Long> {
    Optional<PnlAccount> findByCode(String code);
    List<PnlAccount> findByActiveTrueOrderBySortOrderAscCodeAsc();
}
