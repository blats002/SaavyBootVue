package org.saavy.entity;

import org.saavy.reference.BaseJpaRepository;
import java.util.Optional;

public interface RoleRepository extends BaseJpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
