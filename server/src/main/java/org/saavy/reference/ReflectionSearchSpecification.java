package org.saavy.reference;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionSearchSpecification<T> implements Specification<T> {

    private final String searchTerm;
    private final String skipField;

    public ReflectionSearchSpecification(String search, String skipField) {
        this.searchTerm = search;
        this.skipField = skipField;
    }

    public ReflectionSearchSpecification(String searchTerm) {
        this(searchTerm, null);
    }

    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return cb.conjunction();
        }

        String like = "%" + searchTerm.toLowerCase() + "%";
        List<Predicate> predicates = new ArrayList<>();

        // Search all String fields in the entity
        for (Field field : getAllFields(root.getJavaType())) {
            if (field.getType().equals(String.class)) {
                predicates.add(
                        cb.like(cb.lower(root.get(field.getName())), like)
                );
            }
        }

        // Search OneToOne / ManyToOne String fields
        for (Field field : getAllFields(root.getJavaType())) {

            // Skip the field passed from findByParentIdWithPage
            if (field != null && field.getName().equals(skipField)) {
                continue;
            }

            if (field.isAnnotationPresent(OneToOne.class) ||
                    field.isAnnotationPresent(ManyToOne.class)) {
                Join<Object, Object> join = root.join(field.getName(), JoinType.LEFT);
                for (Field relatedField : getAllFields(field.getType())) {
                    if (relatedField.getType().equals(String.class)) {
                        predicates.add(
                                cb.like(cb.lower(join.get(relatedField.getName())), like)
                        );
                    }
                }
            }
        }

        return cb.or(predicates.toArray(new Predicate[0]));
    }
}
