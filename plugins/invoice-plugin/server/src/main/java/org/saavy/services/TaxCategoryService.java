package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.TaxCategory;
import org.saavy.entity.TaxCategoryDTO;
import org.saavy.entity.TaxCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaxCategoryService {

    @Autowired
    private TaxCategoryRepository taxCategoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<TaxCategoryDTO> getAll() {
        return taxCategoryRepository.findAll().stream()
                .map(cat -> modelMapper.map(cat, TaxCategoryDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<TaxCategoryDTO> getById(Long id) {
        return taxCategoryRepository.findById(id)
                .map(cat -> modelMapper.map(cat, TaxCategoryDTO.class));
    }

    public Optional<TaxCategoryDTO> getByCode(String code) {
        return taxCategoryRepository.findByCode(code)
                .map(cat -> modelMapper.map(cat, TaxCategoryDTO.class));
    }

    public TaxCategoryDTO save(TaxCategoryDTO dto) {
        TaxCategory entity = modelMapper.map(dto, TaxCategory.class);
        if (entity.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        TaxCategory saved = taxCategoryRepository.save(entity);
        return modelMapper.map(saved, TaxCategoryDTO.class);
    }

    public void delete(Long id) {
        taxCategoryRepository.deleteById(id);
    }
}
