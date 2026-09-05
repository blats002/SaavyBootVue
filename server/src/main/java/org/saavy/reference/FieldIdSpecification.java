package org.saavy.reference;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class FieldIdSpecification<T, ID> implements Specification<T> {

    private final String field;
    private final ID id;

    public FieldIdSpecification(String field, ID id) {
        this.field = field;
        this.id = id;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        String fieldName = field;
        return cb.equal(root.get(fieldName).get("id"), id);
    }
}
