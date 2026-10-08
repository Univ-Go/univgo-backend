package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SpaceRepositoryAdapter implements SpaceRepositoryPort {

    private final SpaceJpaRepository spaceJpaRepository;

    public SpaceRepositoryAdapter(SpaceJpaRepository spaceJpaRepository) {
        this.spaceJpaRepository = spaceJpaRepository;
    }

    @Override
    public boolean existsActiveById(UUID id) {
        return spaceJpaRepository.existsActiveById(id);
    }

    @Override
    public Optional<Space> findById(UUID id) {
        return spaceJpaRepository.findById(id).map(SpaceRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Space> findActiveById(UUID id) {
        return spaceJpaRepository.findActiveById(id).map(SpaceRepositoryAdapter::toDomain);
    }

    @Override
    public List<Space> findAllActive() {
        return spaceJpaRepository.findAllActive().stream().map(SpaceRepositoryAdapter::toDomain).toList();
    }

    /**
     * Returns the space it was given rather than re-reading the row. Every column written comes
     * from that object, and {@code category} is not a column at all — it belongs to the type row,
     * which a freshly built entity does not carry. Mapping the saved entity back would therefore
     * need a second query to say something the caller already knows.
     */
    @Override
    public Space save(Space space) {
        spaceJpaRepository.save(toEntity(space));
        return space;
    }

    @Override
    @Transactional
    public void archive(UUID spaceId, UUID actor, LocalDateTime at) {
        spaceJpaRepository.archive(spaceId, at, actor);
    }

    @Override
    @Transactional
    public void restore(UUID spaceId) {
        spaceJpaRepository.restore(spaceId);
    }

    private static SpaceJpaEntity toEntity(Space space) {
        return new SpaceJpaEntity(
                space.getId(),
                space.getName(),
                space.getLocation(),
                space.getCapacity(),
                space.getSpaceTypeId(),
                space.getDescription(),
                space.getRules().toArray(String[]::new));
    }

    private static Space toDomain(SpaceJpaEntity entity) {
        return new Space(
                entity.getId(),
                entity.getName(),
                entity.getLocation(),
                entity.getCapacity(),
                entity.getSpaceTypeId(),
                entity.getSpaceType().getCategory(),
                entity.getDescription(),
                Arrays.asList(entity.getRules()));
    }
}
