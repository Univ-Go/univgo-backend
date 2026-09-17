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
    public void updateMaintenance(UUID id, boolean underMaintenance) {
        spaceJpaRepository.findById(id).ifPresent(entity -> {
            entity.setUnderMaintenance(underMaintenance);
            spaceJpaRepository.save(entity);
        });
    }

    private static Space toDomain(SpaceJpaEntity entity) {
        return new Space(
                entity.getId(),
                entity.getName(),
                entity.getLocation(),
                entity.getCapacity(),
                entity.getSpaceTypeId(),
                entity.getSpaceType().getCategory(),
                entity.isUnderMaintenance());
    }
}
