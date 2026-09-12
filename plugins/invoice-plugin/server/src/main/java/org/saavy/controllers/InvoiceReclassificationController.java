package org.saavy.controllers;

import org.saavy.entity.Invoice;
import org.saavy.services.InvoiceReclassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceReclassificationController {

    @Autowired
    private InvoiceReclassificationService reclassificationService;

    @PostMapping("/{id}/reclassify")
    public ResponseEntity<?> reclassify(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        Long taxCategoryId = payload.get("taxCategoryId");
        if (taxCategoryId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "taxCategoryId is required"));
        }
        return reclassificationService.reclassify(id, taxCategoryId)
                .map(inv -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "invoiceId", inv.getId(),
                        "taxCategoryCode", inv.getTaxCategory().getCode(),
                        "taxCategoryName", inv.getTaxCategory().getName(),
                        "pnlCategory", inv.getTaxCategory().getPnlCategory()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/auto-route")
    public ResponseEntity<?> autoRoute(@PathVariable Long id) {
        return reclassificationService.autoRouteFromVendor(id)
                .map(inv -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "invoiceId", inv.getId(),
                        "taxCategory", inv.getTaxCategory() != null ? inv.getTaxCategory().getName() : "Unassigned"
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
