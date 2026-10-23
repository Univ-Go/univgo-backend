package com.univgo.backend.spaces.application.port.out;

import com.univgo.backend.spaces.domain.SpaceImage;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The photograph rows, ordered by position. One call for the whole catalogue rather than one per
 * space: {@code GetSpaceCatalogService} already reads schedules, reservations and closures this
 * way, for the same reason — the catalogue answers for the whole campus.
 */
public interface SpaceImageRepositoryPort {

    Map<UUID, List<SpaceImage>> findAllBySpaceIds();

    List<SpaceImage> findBySpaceId(UUID spaceId);

    Optional<SpaceImage> findById(UUID imageId);

    SpaceImage save(SpaceImage image);

    /** Writes a whole ordering in one go, which is what a reorder and a compaction both are. */
    void saveAll(List<SpaceImage> images);

    void delete(UUID imageId);

    int countBySpaceId(UUID spaceId);
}
