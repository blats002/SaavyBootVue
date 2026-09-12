package org.saavy.controllers;

import org.saavy.entity.TaxCategoryDTO;
import org.saavy.services.TaxCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tax-categories")
public class TaxCategoryController {

    @Autowired
    private TaxCategoryService taxCategoryService;

    @GetMapping
    public ResponseEntity<List<TaxCategoryDTO>> getAll() {
        return ResponseEntity.ok(taxCategoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaxCategoryDTO> getById(@PathVariable Long id) {
        return taxCategoryService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TaxCategoryDTO> create(@RequestBody TaxCategoryDTO dto) {
        return ResponseEntity.ok(taxCategoryService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxCategoryDTO> update(@PathVariable Long id, @RequestBody TaxCategoryDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(taxCategoryService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taxCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
