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
 *
 * <p>{@code images} is ordered with the cover first, and empty for a space with no photographs
 * uploaded yet — the frontend falls back to its brand placeholder in that case.
 *
 * <p>{@code description} and {@code rules} are what the space says about itself, and they travel
 * with the list rather than only with the detail: the same record answers {@code GET /spaces} and
 * {@code GET /spaces/{id}}, so one shape reaches the frontend instead of two that could drift. At
 * the size of one campus the extra text is a few hundred bytes per space; a catalogue big enough
 * for that to matter wants a leaner list projection, not a second type today.
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
        List<LocalTime> freeBlockStarts,
        List<String> images,
        String description,
        List<String> rules) {
}
