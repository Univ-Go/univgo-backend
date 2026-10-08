package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    /**
     * Field injection, not the constructor: {@code @PersistenceContext} is only processed on fields
     * and setters, so a constructor parameter of this type silently falls back to whatever other
     * {@code EntityManager} bean is on the context rather than Spring's shared, transaction-scoped
     * proxy — which is how the first attempt at this fix produced a Hibernate
     * {@code AssertionFailure: possible non-threadsafe access to session} instead of a working
     * {@code detach}.
     */
    @PersistenceContext
    private EntityManager entityManager;

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
     *
     * <p>{@code detach} matters beyond this method's own return value: {@code spaceType} is
     * deliberately left null on a freshly built entity (see {@link SpaceJpaEntity}'s constructor),
     * and Hibernate's persistence context hands that exact same managed instance back to anyone who
     * reads this id again in the same transaction — {@code join fetch} or not, a query never
     * overwrites an association already in session. {@code CreateSpaceService} does exactly that,
     * reading the space it just created to build its response, and got a {@code NullPointerException}
     * out of it. Detaching forces that later read to hit the database instead of the session cache.
     *
     * <p>{@code saveAndFlush}, not {@code save}: a plain {@code save} only queues the insert, and
     * detaching an entity with a still-pending action leaves Hibernate's action queue pointing at a
     * no-longer-managed instance — the flush that eventually runs it throws {@code AssertionFailure:
     * possible non-threadsafe access to session}. Flushing here executes the insert for real before
     * the entity leaves the session, which is what makes detaching it safe.
     */
    @Override
    public Space save(Space space) {
        SpaceJpaEntity saved = spaceJpaRepository.saveAndFlush(toEntity(space));
        entityManager.detach(saved);
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
