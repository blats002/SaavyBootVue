package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.Party;
import org.saavy.entity.PartyDTO;
import org.saavy.entity.PartyRepository;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartyService extends JPAService<Party, PartyDTO, Long> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PartyRepository partyRepository;

    @Override
    protected BaseJpaRepository<Party, Long> getJpaRepository() {
        return partyRepository;
    }

    @Override
    public PartyDTO toDTO(Party entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, PartyDTO.class);
    }

    @Override
    public Party toEntity(PartyDTO dto, Long id) {
        if (dto == null) return null;
        Party entity = modelMapper.map(dto, Party.class);
        entity.setId(id != null ? id : dto.getId());
        return entity;
    }
}
