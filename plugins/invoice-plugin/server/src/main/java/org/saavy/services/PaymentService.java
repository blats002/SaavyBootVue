package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.*;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService extends JPAService<Payment, PaymentDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Override
    @Transactional
    public PaymentDTO save(PaymentDTO dto) {
        Payment entity = toEntity(dto, null);
        Payment saved = paymentRepository.save(entity);
        updateInvoiceStatus(saved.getInvoice());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public PaymentDTO update(Long id, PaymentDTO dto) {
        Payment entity = toEntity(dto, id);
        Payment saved = paymentRepository.save(entity);
        updateInvoiceStatus(saved.getInvoice());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Optional<Payment> opt = paymentRepository.findById(id);
        if (opt.isPresent()) {
            Payment payment = opt.get();
            Invoice invoice = payment.getInvoice();
            paymentRepository.deleteById(id);
            if (invoice != null && invoice.getId() != null) {
                invoiceRepository.findById(invoice.getId()).ifPresent(inv -> {
                    List<Payment> remainingPayments = paymentRepository.findAllByInvoiceId(inv.getId());
                    inv.recalculateStatus(remainingPayments);
                    invoiceRepository.save(inv);
                });
            }
        } else {
            paymentRepository.deleteById(id);
        }
    }

    private void updateInvoiceStatus(Invoice invoice) {
        if (invoice != null && invoice.getId() != null) {
            invoiceRepository.findById(invoice.getId()).ifPresent(inv -> {
                List<Payment> payments = paymentRepository.findAllByInvoiceId(inv.getId());
                inv.recalculateStatus(payments);
                invoiceRepository.save(inv);
            });
        }
    }

    @Override
    protected BaseJpaRepository<Payment, Long> getJpaRepository() {
        return paymentRepository;
    }

    @Override
    public PaymentDTO toDTO(Payment entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, PaymentDTO.class);
    }

    @Override
    public Payment toEntity(PaymentDTO dto, Long id) {
        if (dto == null) return null;
        Payment payment = modelMapper.map(dto, Payment.class);
        payment.setId(id != null ? id : dto.getId());

        if (dto.getInvoice() != null && dto.getInvoice().getId() != null) {
            invoiceRepository.findById(dto.getInvoice().getId()).ifPresent(payment::setInvoice);
        }

        payment.setFileName(dto.getFileName());
        payment.setContentType(dto.getContentType());
        payment.setContent(dto.getContent());

        return payment;
    }
}
