package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceClosure;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SpaceClosureRepositoryAdapter implements SpaceClosureRepositoryPort {

    private final SpaceClosureJpaRepository spaceClosureJpaRepository;

    public SpaceClosureRepositoryAdapter(SpaceClosureJpaRepository spaceClosureJpaRepository) {
        this.spaceClosureJpaRepository = spaceClosureJpaRepository;
    }

    @Override
    public SpaceClosure save(SpaceClosure closure) {
        return toDomain(spaceClosureJpaRepository.save(toEntity(closure)));
    }

    @Override
    public Optional<SpaceClosure> findById(UUID id) {
        return spaceClosureJpaRepository.findById(id).map(SpaceClosureRepositoryAdapter::toDomain);
    }

    @Override
    public List<SpaceClosure> findBySpaceId(UUID spaceId) {
        return spaceClosureJpaRepository.findBySpaceIdOrderByStartsAtDesc(spaceId).stream()
                .map(SpaceClosureRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<SpaceClosure> findInForceBySpaceId(UUID spaceId) {
        return spaceClosureJpaRepository.findBySpaceIdAndRevertedAtIsNull(spaceId).stream()
                .map(SpaceClosureRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<SpaceClosure> findAllInForce() {
        return spaceClosureJpaRepository.findByRevertedAtIsNull().stream()
                .map(SpaceClosureRepositoryAdapter::toDomain)
                .toList();
    }

    private static SpaceClosureJpaEntity toEntity(SpaceClosure closure) {
        return SpaceClosureJpaEntity.builder()
                .id(closure.getId())
                .spaceId(closure.getSpaceId())
                .startsAt(closure.getStartsAt())
                .endsAt(closure.getEndsAt())
                .reason(closure.getReason())
                .details(closure.getDetails())
                .createdBy(closure.getCreatedBy())
                .createdAt(closure.getCreatedAt())
                .revertedAt(closure.getRevertedAt())
                .revertedBy(closure.getRevertedBy())
                .build();
    }

    private static SpaceClosure toDomain(SpaceClosureJpaEntity entity) {
        return SpaceClosure.builder()
                .id(entity.getId())
                .spaceId(entity.getSpaceId())
                .startsAt(entity.getStartsAt())
                .endsAt(entity.getEndsAt())
                .reason(entity.getReason())
                .details(entity.getDetails())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .revertedAt(entity.getRevertedAt())
                .revertedBy(entity.getRevertedBy())
                .build();
    }
}
