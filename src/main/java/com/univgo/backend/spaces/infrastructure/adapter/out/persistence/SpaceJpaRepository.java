package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceJpaRepository extends JpaRepository<SpaceJpaEntity, UUID> {
}
