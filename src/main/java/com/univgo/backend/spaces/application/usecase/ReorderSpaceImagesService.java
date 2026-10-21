package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.GetSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.in.ReorderSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceImage;
import com.univgo.backend.spaces.domain.SpaceImageView;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Setting the whole order at once, because a partial list leaves the omitted photographs' positions
 * undefined. Position 0 is the cover, which is why there is no separate "set cover" operation.
 *
 * <p>Transactional and relying on {@code space_images_position_unique} being deferrable: renumbering
 * passes through a state where two rows share a position, and the constraint is only checked at
 * commit.
 */
@Service
public class ReorderSpaceImagesService implements ReorderSpaceImagesUseCase {

    private final SpaceImageRepositoryPort spaceImageRepositoryPort;
    private final GetSpaceImagesUseCase getSpaceImagesUseCase;

    public ReorderSpaceImagesService(
            SpaceImageRepositoryPort spaceImageRepositoryPort, GetSpaceImagesUseCase getSpaceImagesUseCase) {
        this.spaceImageRepositoryPort = spaceImageRepositoryPort;
        this.getSpaceImagesUseCase = getSpaceImagesUseCase;
    }

    @Override
    @Transactional
    public List<SpaceImageView> execute(UUID spaceId, List<UUID> imageIds) {
        Map<UUID, SpaceImage> current = spaceImageRepositoryPort.findBySpaceId(spaceId).stream()
                .collect(Collectors.toMap(SpaceImage::id, Function.identity()));

        if (!Set.copyOf(imageIds).equals(current.keySet()) || imageIds.size() != current.size()) {
            throw new IllegalArgumentException(
                    "The order must list every photograph of this space exactly once");
        }

        spaceImageRepositoryPort.saveAll(IntStream.range(0, imageIds.size())
                .mapToObj(position -> current.get(imageIds.get(position)).atPosition(position))
                .toList());

        return getSpaceImagesUseCase.ofSpace(spaceId);
    }
}
