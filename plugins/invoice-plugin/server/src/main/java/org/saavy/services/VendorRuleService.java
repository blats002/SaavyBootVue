package org.saavy.services;

import org.modelmapper.ModelMapper;
import org.saavy.entity.TaxCategoryDTO;
import org.saavy.entity.VendorRule;
import org.saavy.entity.VendorRuleDTO;
import org.saavy.entity.VendorRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VendorRuleService {

    @Autowired
    private VendorRuleRepository vendorRuleRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<VendorRuleDTO> getAll() {
        return vendorRuleRepository.findAll().stream()
                .map(rule -> modelMapper.map(rule, VendorRuleDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<VendorRuleDTO> getById(Long id) {
        return vendorRuleRepository.findById(id)
                .map(rule -> modelMapper.map(rule, VendorRuleDTO.class));
    }

    public Optional<TaxCategoryDTO> matchVendor(String vendorName) {
        if (vendorName == null || vendorName.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = vendorName.trim().toUpperCase();
        List<VendorRule> rules = vendorRuleRepository.findAll();
        for (VendorRule rule : rules) {
            if (rule.getPattern() != null && normalized.contains(rule.getPattern().trim().toUpperCase())) {
                return Optional.ofNullable(rule.getTaxCategory())
                        .map(cat -> modelMapper.map(cat, TaxCategoryDTO.class));
            }
        }
        return Optional.empty();
    }

    public VendorRuleDTO save(VendorRuleDTO dto) {
        VendorRule entity = modelMapper.map(dto, VendorRule.class);
        VendorRule saved = vendorRuleRepository.save(entity);
        return modelMapper.map(saved, VendorRuleDTO.class);
    }

    public void delete(Long id) {
        vendorRuleRepository.deleteById(id);
    }
}
