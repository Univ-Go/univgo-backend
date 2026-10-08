package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceImage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class SpaceImageRepositoryAdapter implements SpaceImageRepositoryPort {

    private final SpaceImageJpaRepository spaceImageJpaRepository;

    public SpaceImageRepositoryAdapter(SpaceImageJpaRepository spaceImageJpaRepository) {
        this.spaceImageJpaRepository = spaceImageJpaRepository;
    }

    @Override
    public Map<UUID, List<SpaceImage>> findAllBySpaceIds() {
        return spaceImageJpaRepository.findAllByOrderBySpaceIdAscPositionAsc().stream()
                .map(SpaceImageRepositoryAdapter::toDomain)
                .collect(Collectors.groupingBy(SpaceImage::spaceId, LinkedHashMap::new, Collectors.toList()));
    }

    @Override
    public List<SpaceImage> findBySpaceId(UUID spaceId) {
        return spaceImageJpaRepository.findBySpaceIdOrderByPositionAsc(spaceId).stream()
                .map(SpaceImageRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<SpaceImage> findById(UUID imageId) {
        return spaceImageJpaRepository.findById(imageId).map(SpaceImageRepositoryAdapter::toDomain);
    }

    @Override
    public SpaceImage save(SpaceImage image) {
        spaceImageJpaRepository.save(toEntity(image));
        return image;
    }

    @Override
    public void saveAll(List<SpaceImage> images) {
        spaceImageJpaRepository.saveAll(images.stream().map(SpaceImageRepositoryAdapter::toEntity).toList());
    }

    @Override
    public void delete(UUID imageId) {
        spaceImageJpaRepository.deleteById(imageId);
    }

    @Override
    public int countBySpaceId(UUID spaceId) {
        return spaceImageJpaRepository.countBySpaceId(spaceId);
    }

    private static SpaceImageJpaEntity toEntity(SpaceImage image) {
        return new SpaceImageJpaEntity(
                image.id(),
                image.spaceId(),
                (short) image.position(),
                image.originalKey(),
                image.contentType(),
                image.width(),
                image.height(),
                image.byteSize(),
                image.createdAt(),
                image.createdBy());
    }

    private static SpaceImage toDomain(SpaceImageJpaEntity entity) {
        return new SpaceImage(
                entity.getId(),
                entity.getSpaceId(),
                entity.getPosition(),
                entity.getOriginalKey(),
                entity.getContentType(),
                entity.getWidth(),
                entity.getHeight(),
                entity.getByteSize(),
                entity.getCreatedAt(),
                entity.getCreatedBy());
    }
}
