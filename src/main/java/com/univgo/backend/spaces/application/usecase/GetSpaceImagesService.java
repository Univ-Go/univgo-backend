package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.GetSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceImageStoragePort;
import com.univgo.backend.spaces.domain.SpaceImage;
import com.univgo.backend.spaces.domain.SpaceImageView;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Puts a fetchable URL on each photograph row. It exists so that nothing which merely renders
 * photographs has to know a bucket is involved: the catalogue and the admin panel both ask for
 * views and get order and URLs already resolved.
 */
@Service
public class GetSpaceImagesService implements GetSpaceImagesUseCase {

    private final SpaceImageRepositoryPort spaceImageRepositoryPort;
    private final SpaceImageStoragePort spaceImageStoragePort;

    public GetSpaceImagesService(
            SpaceImageRepositoryPort spaceImageRepositoryPort, SpaceImageStoragePort spaceImageStoragePort) {
        this.spaceImageRepositoryPort = spaceImageRepositoryPort;
        this.spaceImageStoragePort = spaceImageStoragePort;
    }

    @Override
    public Map<UUID, List<SpaceImageView>> allBySpace() {
        return spaceImageRepositoryPort.findAllBySpaceIds().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> toViews(entry.getValue())));
    }

    @Override
    public List<SpaceImageView> ofSpace(UUID spaceId) {
        return toViews(spaceImageRepositoryPort.findBySpaceId(spaceId));
    }

    private List<SpaceImageView> toViews(List<SpaceImage> images) {
        return images.stream().map(this::toView).toList();
    }

    private SpaceImageView toView(SpaceImage image) {
        return new SpaceImageView(
                image.id(),
                image.position(),
                spaceImageStoragePort.urlsOf(image.originalKey()),
                image.width(),
                image.height());
    }
}
