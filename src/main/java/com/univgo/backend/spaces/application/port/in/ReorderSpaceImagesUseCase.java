package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.domain.SpaceImageView;
import java.util.List;
import java.util.UUID;

public interface ReorderSpaceImagesUseCase {

    /** {@code imageIds} must be a permutation of the space's current photographs. */
    List<SpaceImageView> execute(UUID spaceId, List<UUID> imageIds);
}
