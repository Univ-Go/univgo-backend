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
        return new SpaceClosureJpaEntity(
                closure.getId(),
                closure.getSpaceId(),
                closure.getStartsAt(),
                closure.getEndsAt(),
                closure.getReason(),
                closure.getDetails(),
                closure.getCreatedBy(),
                closure.getCreatedAt(),
                closure.getRevertedAt(),
                closure.getRevertedBy());
    }

    private static SpaceClosure toDomain(SpaceClosureJpaEntity entity) {
        return new SpaceClosure(
                entity.getId(),
                entity.getSpaceId(),
                entity.getStartsAt(),
                entity.getEndsAt(),
                entity.getReason(),
                entity.getDetails(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getRevertedAt(),
                entity.getRevertedBy());
    }
}
