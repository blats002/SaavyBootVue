package org.saavy.entity;

import jakarta.persistence.Entity;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvoiceFileRepository extends BaseJpaRepository<InvoiceFile, Long> {
//    List<InvoiceFile> findAllByInvoiceId(Long id);
//    List<InvoiceFile> findAllByInvoiceId(Long id, Specification spec);
//    Page<InvoiceFile> findAllByInvoiceId(Long id, Specification spec, Pageable pageable);
}
