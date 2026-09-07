package org.saavy.entity;

import org.saavy.reference.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleItemRepository extends BaseJpaRepository<SampleItem, Long> {
}
