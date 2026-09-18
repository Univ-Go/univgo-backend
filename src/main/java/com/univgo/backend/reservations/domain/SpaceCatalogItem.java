package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.SpaceCategory;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * What the "elegir el espacio" catalog screen shows per space.
 *
 * <p>{@code freeBlockStarts} answers both halves of the question the catalog asks — whether the
 * space has room on the requested day and from what time — with one list instead of a flag and a
 * time that could disagree.
 *
 * <p>An empty list on its own cannot say why, and the three whys are different things to a person
 * deciding where to walk: the space does not open that day, it is shut, or every block is taken.
 * {@code opensOnDate} and {@code closedOnDate} are what tell them apart.
 */
public record SpaceCatalogItem(
        UUID spaceId,
        String name,
        String location,
        SpaceCategory category,
        int capacity,
        boolean underMaintenance,
        boolean opensOnDate,
        boolean closedOnDate,
        List<LocalTime> freeBlockStarts) {
}
