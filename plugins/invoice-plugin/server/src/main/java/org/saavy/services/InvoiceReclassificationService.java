package org.saavy.services;

import org.saavy.entity.Invoice;
import org.saavy.entity.InvoiceRepository;
import org.saavy.entity.TaxCategory;
import org.saavy.entity.TaxCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InvoiceReclassificationService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private TaxCategoryRepository taxCategoryRepository;

    @Autowired
    private VendorRuleService vendorRuleService;

    @Transactional
    public Optional<Invoice> reclassify(Long invoiceId, Long newTaxCategoryId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<TaxCategory> catOpt = taxCategoryRepository.findById(newTaxCategoryId);
        if (catOpt.isEmpty()) {
            return Optional.empty();
        }

        Invoice invoice = invoiceOpt.get();
        TaxCategory cat = catOpt.get();
        invoice.setTaxCategory(cat);
        invoice.setIsDeductible(cat.getIsDeductible());
        return Optional.of(invoiceRepository.save(invoice));
    }

    @Transactional
    public Optional<Invoice> autoRouteFromVendor(Long invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty() || invoiceOpt.get().getParty() == null) {
            return invoiceOpt;
        }

        Invoice invoice = invoiceOpt.get();
        String vendorName = invoice.getParty().getName();
        vendorRuleService.matchVendor(vendorName).ifPresent(dto -> {
            taxCategoryRepository.findById(dto.getId()).ifPresent(cat -> {
                invoice.setTaxCategory(cat);
                invoice.setIsDeductible(cat.getIsDeductible());
                invoiceRepository.save(invoice);
            });
        });

        return Optional.of(invoice);
    }
}
