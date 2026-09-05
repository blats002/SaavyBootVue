package org.saavy.controllers;

import org.saavy.component.ParentFieldRegistry;
import org.saavy.services.JPAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Base controller for common CRUD operations
 */
public abstract class BaseController<E, D, ID> {

    @Autowired
    private ParentFieldRegistry parentFieldRegistry;

    protected abstract JPAService<E, D, ID> getService();

    @GetMapping
    public List<D> getAll() {
        return getService().findAll();
    }

    @GetMapping("/page")
    public Page<D> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int rows,
            @RequestParam(defaultValue = "") String search
    ) {
        return getService().findAll(page, rows, search);
    }

    @GetMapping("/by-{field}/{id}")
    public List<D> findByParentId(
            @PathVariable String field,
            @PathVariable Long id
    ) {
        return (List<D>) getService().findByParentId(field, (ID) id);
    }

    @GetMapping("/by-{field}/{id}/page")
    public Page<D> findByParentIdWithPage(
            @PathVariable String field,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int rows,
            @RequestParam(defaultValue = "") String search
    ) {
        return getService().findByParentIdWithPage(field, (ID) id, page, rows, search);
    }

    @GetMapping("/{id}")
    public D getById(@PathVariable ID id) {
        Optional<D> entity = getService().findById(id);
        return entity.orElse(null);
    }

    @PostMapping
    public D save(@RequestBody D dto) {
        return getService().save(dto);
    }

    @PutMapping("/{id}")
    public D update(@RequestBody D dto, @PathVariable ID id) {
        return getService().update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable ID id) {
        getService().deleteById(id);
    }
}
