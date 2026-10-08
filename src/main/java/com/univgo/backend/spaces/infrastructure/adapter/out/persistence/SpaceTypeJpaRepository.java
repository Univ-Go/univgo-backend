package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceTypeJpaRepository extends JpaRepository<SpaceTypeJpaEntity, UUID> {

    List<SpaceTypeJpaEntity> findAllByOrderByNameAsc();
}
