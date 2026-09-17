package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceScheduleJpaRepository extends JpaRepository<SpaceScheduleJpaEntity, UUID> {

    List<SpaceScheduleJpaEntity> findBySpaceIdAndDayOfWeek(UUID spaceId, short dayOfWeek);

    List<SpaceScheduleJpaEntity> findByDayOfWeek(short dayOfWeek);

    List<SpaceScheduleJpaEntity> findBySpaceId(UUID spaceId);
}
