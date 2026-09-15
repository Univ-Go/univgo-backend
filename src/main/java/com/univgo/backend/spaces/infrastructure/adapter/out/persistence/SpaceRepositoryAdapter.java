package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SpaceRepositoryAdapter implements SpaceRepositoryPort {

    private final SpaceJpaRepository spaceJpaRepository;

    public SpaceRepositoryAdapter(SpaceJpaRepository spaceJpaRepository) {
        this.spaceJpaRepository = spaceJpaRepository;
    }

    @Override
    public boolean existsById(UUID id) {
        return spaceJpaRepository.existsById(id);
    }

    @Override
    public Optional<Space> findById(UUID id) {
        return spaceJpaRepository.findById(id).map(SpaceRepositoryAdapter::toDomain);
    }

    @Override
    public List<Space> findAll() {
        return spaceJpaRepository.findAll().stream().map(SpaceRepositoryAdapter::toDomain).toList();
    }

    @Override
    public Space save(Space space) {
        var entity = new SpaceJpaEntity(
                space.getId(), space.getName(), space.getCapacity(), space.getSpaceTypeId(), space.isUnderMaintenance());
        return toDomain(spaceJpaRepository.save(entity));
    }

    private static Space toDomain(SpaceJpaEntity entity) {
        return new Space(
                entity.getId(), entity.getName(), entity.getCapacity(), entity.getSpaceTypeId(), entity.isUnderMaintenance());
    }
}
