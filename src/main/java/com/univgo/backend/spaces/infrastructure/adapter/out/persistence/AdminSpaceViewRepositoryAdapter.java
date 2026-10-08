package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.AdminSpaceViewRepositoryPort;
import com.univgo.backend.spaces.domain.AdminSpaceView;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class AdminSpaceViewRepositoryAdapter implements AdminSpaceViewRepositoryPort {

    private final SpaceJpaRepository spaceJpaRepository;

    public AdminSpaceViewRepositoryAdapter(SpaceJpaRepository spaceJpaRepository) {
        this.spaceJpaRepository = spaceJpaRepository;
    }

    @Override
    public List<AdminSpaceView> findAll(boolean includeArchived) {
        List<SpaceJpaEntity> entities =
                includeArchived ? spaceJpaRepository.findAllForAdmin() : spaceJpaRepository.findAllActive();
        return entities.stream().map(AdminSpaceViewRepositoryAdapter::toView).toList();
    }

    @Override
    public Optional<AdminSpaceView> findById(UUID spaceId) {
        return spaceJpaRepository.findByIdForAdmin(spaceId).map(AdminSpaceViewRepositoryAdapter::toView);
    }

    private static AdminSpaceView toView(SpaceJpaEntity entity) {
        return new AdminSpaceView(
                entity.getId(),
                entity.getName(),
                entity.getLocation(),
                entity.getSpaceTypeId(),
                entity.getSpaceType().getName(),
                entity.getSpaceType().getCategory(),
                entity.getCapacity(),
                entity.getDescription(),
                Arrays.asList(entity.getRules()),
                entity.getArchivedAt());
    }
}
