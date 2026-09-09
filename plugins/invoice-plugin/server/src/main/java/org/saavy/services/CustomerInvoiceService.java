package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.*;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerInvoiceService extends JPAService<CustomerInvoice, CustomerInvoiceDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CustomerInvoiceRepository customerInvoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Override
    protected BaseJpaRepository<CustomerInvoice, Long> getJpaRepository() {
        return customerInvoiceRepository;
    }

    @Override
    @Transactional
    public CustomerInvoiceDTO save(CustomerInvoiceDTO dto) {
        CustomerInvoice invoice = toEntity(dto, null);
        invoice.recalculateStatus();
        return toDTO(customerInvoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public CustomerInvoiceDTO update(Long id, CustomerInvoiceDTO dto) {
        CustomerInvoice invoice = toEntity(dto, id);
        List<Payment> existingPayments = paymentRepository.findAllByInvoiceId(id);
        invoice.recalculateStatus(existingPayments);
        return toDTO(customerInvoiceRepository.save(invoice));
    }

    // Mapping: Entity -> DTO
    public CustomerInvoiceDTO toDTO(CustomerInvoice invoice) {
        if (invoice == null) return null;
        return modelMapper.map(invoice, CustomerInvoiceDTO.class);
    }

    // Mapping: DTO -> Entity
    public CustomerInvoice toEntity(CustomerInvoiceDTO dto, Long id) {
        if (dto == null) return null;
        CustomerInvoice invoice = modelMapper.map(dto, CustomerInvoice.class);
        invoice.setId(id != null ? id : dto.getId());
        if (dto.getParty() != null && dto.getParty().getId() != null) {
            partyRepository.findById(dto.getParty().getId()).ifPresent(invoice::setParty);
        }
        return invoice;
    }
}
