package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceImageJpaRepository extends JpaRepository<SpaceImageJpaEntity, UUID> {

    List<SpaceImageJpaEntity> findAllByOrderBySpaceIdAscPositionAsc();

    List<SpaceImageJpaEntity> findBySpaceIdOrderByPositionAsc(UUID spaceId);

    int countBySpaceId(UUID spaceId);
}
