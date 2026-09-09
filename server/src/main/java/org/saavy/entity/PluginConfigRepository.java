package org.saavy.entity;

import org.saavy.reference.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PluginConfigRepository extends BaseJpaRepository<PluginConfig, Long> {
    Optional<PluginConfig> findByName(String name);
    List<PluginConfig> findByEnabledTrue();
}
