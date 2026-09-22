package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceClosureJpaRepository extends JpaRepository<SpaceClosureJpaEntity, UUID> {

    List<SpaceClosureJpaEntity> findBySpaceIdOrderByStartsAtDesc(UUID spaceId);

    List<SpaceClosureJpaEntity> findBySpaceIdAndRevertedAtIsNull(UUID spaceId);

    List<SpaceClosureJpaEntity> findByRevertedAtIsNull();
}
