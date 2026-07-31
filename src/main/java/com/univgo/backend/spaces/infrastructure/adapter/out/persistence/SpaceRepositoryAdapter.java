package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
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
}
