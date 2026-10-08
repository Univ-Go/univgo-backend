package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceImageStoragePort;
import com.univgo.backend.spaces.application.port.in.DeleteSpaceImageUseCase;
import com.univgo.backend.spaces.domain.LastImageException;
import com.univgo.backend.spaces.domain.SpaceImage;
import com.univgo.backend.spaces.domain.SpaceImageNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Removing a photograph closes the gap it leaves: positions are renumbered to {@code 0..n-1} so
 * that "position 0 is the cover" stays true and a later reorder has no holes to reason about.
 */
@Service
public class DeleteSpaceImageService implements DeleteSpaceImageUseCase {

    private final SpaceImageRepositoryPort spaceImageRepositoryPort;
    private final SpaceImageStoragePort spaceImageStoragePort;

    public DeleteSpaceImageService(
            SpaceImageRepositoryPort spaceImageRepositoryPort, SpaceImageStoragePort spaceImageStoragePort) {
        this.spaceImageRepositoryPort = spaceImageRepositoryPort;
        this.spaceImageStoragePort = spaceImageStoragePort;
    }

    @Override
    @Transactional
    public void execute(UUID spaceId, UUID imageId) {
        List<SpaceImage> images = spaceImageRepositoryPort.findBySpaceId(spaceId);

        SpaceImage target = images.stream()
                .filter(image -> image.id().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new SpaceImageNotFoundException(imageId));

        if (images.size() == 1) {
            throw new LastImageException();
        }

        spaceImageRepositoryPort.delete(imageId);

        List<SpaceImage> remaining = images.stream()
                .filter(image -> !image.id().equals(imageId))
                .toList();
        spaceImageRepositoryPort.saveAll(IntStream.range(0, remaining.size())
                .mapToObj(position -> remaining.get(position).atPosition(position))
                .toList());

        spaceImageStoragePort.deletePrefix(target.originalKey());
    }
}
