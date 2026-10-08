package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SpaceScheduleRepositoryAdapter implements SpaceScheduleRepositoryPort {

    private final SpaceScheduleJpaRepository spaceScheduleJpaRepository;

    public SpaceScheduleRepositoryAdapter(SpaceScheduleJpaRepository spaceScheduleJpaRepository) {
        this.spaceScheduleJpaRepository = spaceScheduleJpaRepository;
    }

    @Override
    public List<SpaceSchedule> findBySpaceIdAndDayOfWeek(UUID spaceId, int dayOfWeek) {
        return toDomain(spaceScheduleJpaRepository.findBySpaceIdAndDayOfWeek(spaceId, (short) dayOfWeek));
    }

    @Override
    public List<SpaceSchedule> findByDayOfWeek(int dayOfWeek) {
        return toDomain(spaceScheduleJpaRepository.findByDayOfWeek((short) dayOfWeek));
    }

    @Override
    public Map<UUID, List<SpaceSchedule>> findAllBySpaceIds() {
        return toDomain(spaceScheduleJpaRepository.findAll()).stream()
                .collect(Collectors.groupingBy(SpaceSchedule::getSpaceId));
    }

    @Override
    public List<SpaceSchedule> findBySpaceId(UUID spaceId) {
        return toDomain(spaceScheduleJpaRepository.findBySpaceId(spaceId));
    }

    /**
     * Transactional for the same reason the refresh-token adapter's deletes are: the delete and the
     * inserts are one change. Halfway through, a space has lost windows it should still have.
     */
    @Override
    @Transactional
    public void replaceAll(UUID spaceId, List<SpaceSchedule> windows) {
        spaceScheduleJpaRepository.deleteBySpaceId(spaceId);
        spaceScheduleJpaRepository.flush();
        spaceScheduleJpaRepository.saveAll(windows.stream()
                .map(window -> new SpaceScheduleJpaEntity(
                        window.getId(),
                        window.getSpaceId(),
                        (short) window.getDayOfWeek(),
                        window.getStartTime(),
                        window.getEndTime()))
                .toList());
    }

    private static List<SpaceSchedule> toDomain(List<SpaceScheduleJpaEntity> entities) {
        return entities.stream()
                .map(entity -> new SpaceSchedule(
                        entity.getId(), entity.getSpaceId(), entity.getDayOfWeek(), entity.getStartTime(), entity.getEndTime()))
                .toList();
    }
}
