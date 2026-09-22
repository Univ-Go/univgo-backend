package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One space read the way the catalog reads it, for the detail screen and for the booking flow. It
 * answers with the same {@link SpaceCatalogItem} as the list because the question is the same one —
 * what this space is and whether it has room on a given day — and a second shape would only be a
 * second thing to keep in step.
 */
public interface GetSpaceDetailUseCase {

    /** @throws com.univgo.backend.spaces.domain.SpaceNotFoundException if no space carries that id. */
    SpaceCatalogItem execute(UUID spaceId, LocalDate date);
}
