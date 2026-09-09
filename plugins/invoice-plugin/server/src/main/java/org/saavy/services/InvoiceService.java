package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.*;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService extends JPAService<Invoice, InvoiceDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Override
    protected BaseJpaRepository<Invoice, Long> getJpaRepository() {
        return invoiceRepository;
    }

    @Override
    public InvoiceDTO save(InvoiceDTO dto) {
        dto.setOutstandingAmount(dto.getTotalAmount());
        return super.save(dto);
    }

    // Mapping: Entity -> DTO
    public InvoiceDTO toDTO(Invoice invoice) {
        if (invoice == null) return null;
        return modelMapper.map(invoice, InvoiceDTO.class);
    }

    // Mapping: DTO -> Entity
    public Invoice toEntity(InvoiceDTO dto, Long id) {
        if (dto == null) return null;
        Invoice invoice = modelMapper.map(dto, Invoice.class);
        invoice.setId(id != null ? id : dto.getId());
        if (dto.getParty() != null && dto.getParty().getId() != null) {
            partyRepository.findById(dto.getParty().getId()).ifPresent(invoice::setParty);
        }
        return invoice;
    }
}
