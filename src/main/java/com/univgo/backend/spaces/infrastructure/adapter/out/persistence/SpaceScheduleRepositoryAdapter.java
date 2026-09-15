package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SpaceScheduleRepositoryAdapter implements SpaceScheduleRepositoryPort {

    private final SpaceScheduleJpaRepository spaceScheduleJpaRepository;

    public SpaceScheduleRepositoryAdapter(SpaceScheduleJpaRepository spaceScheduleJpaRepository) {
        this.spaceScheduleJpaRepository = spaceScheduleJpaRepository;
    }

    @Override
    public List<SpaceSchedule> findBySpaceIdAndDayOfWeek(UUID spaceId, int dayOfWeek) {
        return spaceScheduleJpaRepository.findBySpaceIdAndDayOfWeek(spaceId, (short) dayOfWeek).stream()
                .map(entity -> new SpaceSchedule(
                        entity.getId(), entity.getSpaceId(), entity.getDayOfWeek(), entity.getStartTime(), entity.getEndTime()))
                .toList();
    }
}
