package org.saavy.entity;

import org.saavy.reference.BaseJpaRepository;
import java.util.Optional;

public interface UserRepository extends BaseJpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
