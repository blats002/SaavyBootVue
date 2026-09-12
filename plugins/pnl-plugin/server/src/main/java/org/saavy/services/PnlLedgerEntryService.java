package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.PnlAccountRepository;
import org.saavy.entity.PnlLedgerEntry;
import org.saavy.entity.PnlLedgerEntryDTO;
import org.saavy.entity.PnlLedgerEntryRepository;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PnlLedgerEntryService extends JPAService<PnlLedgerEntry, PnlLedgerEntryDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PnlLedgerEntryRepository pnlLedgerEntryRepository;

    @Autowired
    private PnlAccountRepository pnlAccountRepository;

    @Override
    protected BaseJpaRepository<PnlLedgerEntry, Long> getJpaRepository() {
        return pnlLedgerEntryRepository;
    }

    public List<PnlLedgerEntryDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return pnlLedgerEntryRepository.findByEntryDateBetweenWithAccount(startDate, endDate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PnlLedgerEntryDTO> findByAccountIdAndDateRange(Long accountId, LocalDate startDate, LocalDate endDate) {
        return pnlLedgerEntryRepository.findByAccountIdAndEntryDateBetweenWithAccount(accountId, startDate, endDate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PnlLedgerEntryDTO toDTO(PnlLedgerEntry entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, PnlLedgerEntryDTO.class);
    }

    @Override
    public PnlLedgerEntry toEntity(PnlLedgerEntryDTO dto, Long id) {
        if (dto == null) return null;
        PnlLedgerEntry entity = modelMapper.map(dto, PnlLedgerEntry.class);
        entity.setId(id != null ? id : dto.getId());
        if (dto.getAccount() != null && dto.getAccount().getId() != null) {
            pnlAccountRepository.findById(dto.getAccount().getId()).ifPresent(entity::setAccount);
        }
        return entity;
    }
}
