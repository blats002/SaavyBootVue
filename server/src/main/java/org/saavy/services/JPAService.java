package org.saavy.services;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface JPAService<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T save(T entity);
    T update(ID id, T entity);
    void deleteById(ID id);
    public default List<T> findByParentId(String field, ID id) {
        return new ArrayList<T>();
    }
}
