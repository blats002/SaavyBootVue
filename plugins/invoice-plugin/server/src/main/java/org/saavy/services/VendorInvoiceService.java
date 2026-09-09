package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.*;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VendorInvoiceService extends JPAService<VendorInvoice, VendorInvoiceDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private VendorInvoiceRepository vendorInvoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Override
    protected BaseJpaRepository<VendorInvoice, Long> getJpaRepository() {
        return vendorInvoiceRepository;
    }

    @Override
    @Transactional
    public VendorInvoiceDTO save(VendorInvoiceDTO dto) {
        VendorInvoice invoice = toEntity(dto, null);
        invoice.recalculateStatus();
        return toDTO(vendorInvoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public VendorInvoiceDTO update(Long id, VendorInvoiceDTO dto) {
        VendorInvoice invoice = toEntity(dto, id);
        List<Payment> existingPayments = paymentRepository.findAllByInvoiceId(id);
        invoice.recalculateStatus(existingPayments);
        return toDTO(vendorInvoiceRepository.save(invoice));
    }

    // Mapping: Entity -> DTO
    public VendorInvoiceDTO toDTO(VendorInvoice invoice) {
        if (invoice == null) return null;
        return modelMapper.map(invoice, VendorInvoiceDTO.class);
    }

    // Mapping: DTO -> Entity
    public VendorInvoice toEntity(VendorInvoiceDTO dto, Long id) {
        if (dto == null) return null;
        VendorInvoice invoice = modelMapper.map(dto, VendorInvoice.class);
        invoice.setId(id != null ? id : dto.getId());
        if (dto.getParty() != null && dto.getParty().getId() != null) {
            partyRepository.findById(dto.getParty().getId()).ifPresent(invoice::setParty);
        }
        return invoice;
    }
}
