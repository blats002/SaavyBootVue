package org.saavy.entity;

import org.saavy.reference.BaseJpaRepository;

import java.util.List;

public interface PaymentRepository extends BaseJpaRepository<Payment, Long> {
    List<Payment> findAllByInvoiceId(Long invoiceId);
}
