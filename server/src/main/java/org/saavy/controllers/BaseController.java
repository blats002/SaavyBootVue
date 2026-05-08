
package org.saavy.controllers;

import org.saavy.component.ParentFieldRegistry;
import org.saavy.services.JPAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base controller for common CRUD operations
 */
public abstract class BaseController<T, ID> {

    @Autowired
    private ParentFieldRegistry parentFieldRegistry;

    protected abstract JPAService<T, ID> getService();

    @GetMapping
    public List<T> getAll() {
        return getService().findAll();
    }

    @GetMapping("/by-{field}/{id}")
    public List<T> findByParentId(
            @PathVariable String field,
            @PathVariable Long id
    ) {
        return (List<T>) getService().findByParentId(field, (ID) id);
    }


    private Specification<T> byParent(String field, Long id) {
        return (root, query, cb) ->
                cb.equal(root.get(field).get("id"), id);
    }


    @GetMapping("/{id}")
    public T getById(@PathVariable ID id) {
        Optional<T> entity = getService().findById(id);
        return entity.orElse(null);
    }

    @PostMapping
    public T save(@RequestBody T entity) {
        return getService().save(entity);
    }

    @PutMapping("/{id}")
    public T update(@RequestBody T entity, @PathVariable ID id) {
        return getService().update(id, entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable ID id) {
        getService().deleteById(id);
    }
}
