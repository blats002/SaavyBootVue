package org.saavy.entity;

import org.saavy.reference.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PnlLedgerEntryRepository extends BaseJpaRepository<PnlLedgerEntry, Long> {

    @Query("SELECT e FROM PnlLedgerEntry e JOIN FETCH e.account WHERE e.entryDate BETWEEN :startDate AND :endDate ORDER BY e.entryDate ASC")
    List<PnlLedgerEntry> findByEntryDateBetweenWithAccount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM PnlLedgerEntry e JOIN FETCH e.account WHERE e.account.id = :accountId AND e.entryDate BETWEEN :startDate AND :endDate ORDER BY e.entryDate ASC")
    List<PnlLedgerEntry> findByAccountIdAndEntryDateBetweenWithAccount(@Param("accountId") Long accountId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<PnlLedgerEntry> findByEntryDateBetween(LocalDate startDate, LocalDate endDate);
}
