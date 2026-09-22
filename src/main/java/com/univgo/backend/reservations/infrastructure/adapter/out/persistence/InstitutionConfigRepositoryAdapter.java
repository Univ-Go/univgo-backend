package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Repository;

/**
 * The singleton row changes only through {@link #update}, which every admin call routes through —
 * so a request-scoped read can cache it in memory instead of paying a DB round trip on every one
 * of the many call sites that need it just to compute a block.
 */
@Repository
public class InstitutionConfigRepositoryAdapter implements InstitutionConfigRepositoryPort {

    private static final short SINGLETON_ID = 1;

    private final InstitutionConfigJpaRepository institutionConfigJpaRepository;
    private final AtomicReference<InstitutionConfig> cache = new AtomicReference<>();

    public InstitutionConfigRepositoryAdapter(InstitutionConfigJpaRepository institutionConfigJpaRepository) {
        this.institutionConfigJpaRepository = institutionConfigJpaRepository;
    }

    @Override
    public InstitutionConfig getCurrent() {
        InstitutionConfig cached = cache.get();
        if (cached != null) {
            return cached;
        }
        InstitutionConfigJpaEntity entity = institutionConfigJpaRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("institution_config row is missing"));
        InstitutionConfig loaded = toDomain(entity);
        cache.set(loaded);
        return loaded;
    }

    @Override
    public InstitutionConfig update(InstitutionConfig config) {
        InstitutionConfigJpaEntity entity = new InstitutionConfigJpaEntity(
                config.getBlockDurationMinutes(),
                config.getCheckInToleranceMinutes(),
                config.getMinUsageMinutes(),
                config.getReservationsPerSpacePerDay(),
                LocalDateTime.now());
        InstitutionConfig updated = toDomain(institutionConfigJpaRepository.save(entity));
        cache.set(updated);
        return updated;
    }

    private static InstitutionConfig toDomain(InstitutionConfigJpaEntity entity) {
        return new InstitutionConfig(
                entity.getBlockDurationMinutes(),
                entity.getCheckInToleranceMinutes(),
                entity.getMinUsageMinutes(),
                entity.getReservationsPerSpacePerDay());
    }
}
