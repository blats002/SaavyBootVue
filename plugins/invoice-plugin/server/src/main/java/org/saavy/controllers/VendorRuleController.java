package org.saavy.controllers;

import org.saavy.entity.TaxCategoryDTO;
import org.saavy.entity.VendorRuleDTO;
import org.saavy.services.VendorRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor-rules")
public class VendorRuleController {

    @Autowired
    private VendorRuleService vendorRuleService;

    @GetMapping
    public ResponseEntity<List<VendorRuleDTO>> getAll() {
        return ResponseEntity.ok(vendorRuleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorRuleDTO> getById(@PathVariable Long id) {
        return vendorRuleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/match")
    public ResponseEntity<?> matchVendor(@RequestParam String vendorName) {
        return vendorRuleService.matchVendor(vendorName)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of("matched", false, "message", "No matching rule found")));
    }

    @PostMapping
    public ResponseEntity<VendorRuleDTO> create(@RequestBody VendorRuleDTO dto) {
        return ResponseEntity.ok(vendorRuleService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorRuleDTO> update(@PathVariable Long id, @RequestBody VendorRuleDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(vendorRuleService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vendorRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
