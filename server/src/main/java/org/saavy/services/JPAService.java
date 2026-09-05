package org.saavy.services;

import org.saavy.reference.BaseJpaRepository;
import org.saavy.reference.FieldIdSpecification;
import org.saavy.reference.ReflectionSearchSpecification;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public abstract class JPAService<E, D, ID> {

    protected abstract BaseJpaRepository<E, ID> getJpaRepository();

    public List<D> findAll() {
        return getJpaRepository().findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<D> findAll(int page, int rows, String search) {
        Pageable pageable = PageRequest.of(page, rows);
        Specification<E> spec = new ReflectionSearchSpecification<>(search);
        return getJpaRepository().findAll(spec, pageable)
                .map(this::toDTO);
    }

    public Optional<D> findById(ID id) {
        return getJpaRepository().findById(id)
                .map(this::toDTO);
    }

    public D save(D dto) {
        return toDTO(getJpaRepository().save(toEntity(dto, null)));
    }

    public D update(ID id, D dto) {
        return toDTO(getJpaRepository().save(toEntity(dto, id)));
    }

    public void deleteById(ID id) {
        getJpaRepository().deleteById(id);
    }

    public List<D> findByParentId(String field, ID id) {
        Specification<E> spec = new FieldIdSpecification<>(field, (Long) id);
        return getJpaRepository().findAll(spec).stream()
                .map(this::toDTO)
                .toList();
    }

    public Page<D> findByParentIdWithPage(String field, ID id, int page, int rows, String search) {
        Pageable pageable = PageRequest.of(page, rows);
        Specification<E> spec = (Specification<E>) Specification.unrestricted()
                .and((Specification<Object>) new FieldIdSpecification<E, ID>(field, id))
                .and((Specification<Object>) new ReflectionSearchSpecification<E>(search, field));
        return getJpaRepository().findAll(spec, pageable)
                .map(this::toDTO);
    }

    public abstract D toDTO(E entity);
    public abstract E toEntity(D dto, ID id);
}
