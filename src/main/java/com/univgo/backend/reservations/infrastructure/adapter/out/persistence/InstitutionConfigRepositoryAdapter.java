package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

@Repository
public class InstitutionConfigRepositoryAdapter implements InstitutionConfigRepositoryPort {

    private static final short SINGLETON_ID = 1;

    private final InstitutionConfigJpaRepository institutionConfigJpaRepository;

    public InstitutionConfigRepositoryAdapter(InstitutionConfigJpaRepository institutionConfigJpaRepository) {
        this.institutionConfigJpaRepository = institutionConfigJpaRepository;
    }

    @Override
    public InstitutionConfig getCurrent() {
        InstitutionConfigJpaEntity entity = institutionConfigJpaRepository.findById(SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("institution_config row is missing"));
        return toDomain(entity);
    }

    @Override
    public InstitutionConfig update(InstitutionConfig config) {
        InstitutionConfigJpaEntity entity = new InstitutionConfigJpaEntity(
                config.getBlockDurationMinutes(),
                config.getCheckInToleranceMinutes(),
                config.getMinUsageMinutes(),
                config.getReservationsPerSpacePerDay(),
                LocalDateTime.now());
        return toDomain(institutionConfigJpaRepository.save(entity));
    }

    private static InstitutionConfig toDomain(InstitutionConfigJpaEntity entity) {
        return new InstitutionConfig(
                entity.getBlockDurationMinutes(),
                entity.getCheckInToleranceMinutes(),
                entity.getMinUsageMinutes(),
                entity.getReservationsPerSpacePerDay());
    }
}
