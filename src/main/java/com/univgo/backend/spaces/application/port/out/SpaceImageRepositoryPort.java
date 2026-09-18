package com.univgo.backend.spaces.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Every space's photograph URLs, keyed by space id and ordered with the cover first. One call for
 * the whole catalogue rather than one per space: {@code GetSpaceCatalogService} already reads
 * schedules, reservations and closures this way, for the same reason — the catalogue answers for
 * the whole campus, and asking space by space is where the seconds went.
 */
public interface SpaceImageRepositoryPort {

    Map<UUID, List<String>> findAllImageUrls();
}
