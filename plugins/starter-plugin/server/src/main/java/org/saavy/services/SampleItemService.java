package org.saavy.services;

import org.saavy.entity.SampleItem;
import org.saavy.entity.SampleItemDTO;
import org.saavy.entity.SampleItemRepository;
import org.saavy.reference.BaseJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SampleItemService extends JPAService<SampleItem, SampleItemDTO, Long> {

    @Autowired
    private SampleItemRepository sampleItemRepository;

    @Override
    protected BaseJpaRepository<SampleItem, Long> getJpaRepository() {
        return sampleItemRepository;
    }

    @Override
    public SampleItemDTO toDTO(SampleItem entity) {
        SampleItemDTO dto = new SampleItemDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setActive(entity.getActive());
        return dto;
    }

    @Override
    public SampleItem toEntity(SampleItemDTO dto, Long id) {
        SampleItem item = new SampleItem();
        item.setId(id != null ? id : dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setActive(dto.getActive() != null ? dto.getActive() : true);
        return item;
    }
}
