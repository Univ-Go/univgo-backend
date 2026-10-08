package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.domain.SpaceImageView;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GetSpaceImagesUseCase {

    /** Every space's photographs, in order. One read for the whole catalogue. */
    Map<UUID, List<SpaceImageView>> allBySpace();

    List<SpaceImageView> ofSpace(UUID spaceId);
}
