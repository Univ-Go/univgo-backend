package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.SpaceTypeRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SpaceTypeRepositoryAdapter implements SpaceTypeRepositoryPort {

    private final SpaceTypeJpaRepository spaceTypeJpaRepository;

    public SpaceTypeRepositoryAdapter(SpaceTypeJpaRepository spaceTypeJpaRepository) {
        this.spaceTypeJpaRepository = spaceTypeJpaRepository;
    }

    @Override
    public List<SpaceType> findAll() {
        return spaceTypeJpaRepository.findAllByOrderByNameAsc().stream()
                .map(SpaceTypeRepositoryAdapter::toDomain)
                .toList();
    }

    private static SpaceType toDomain(SpaceTypeJpaEntity entity) {
        return new SpaceType(entity.getId(), entity.getName(), entity.getCategory());
    }

    @Override
    public Optional<SpaceType> findById(UUID id) {
        return spaceTypeJpaRepository.findById(id).map(SpaceTypeRepositoryAdapter::toDomain);
    }
}
