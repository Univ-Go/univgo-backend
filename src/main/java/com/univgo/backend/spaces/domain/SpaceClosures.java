package com.univgo.backend.spaces.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The closures in force, indexed by space. Every read that decides whether a space can be used goes
 * through this — the catalog, one space's availability, creating a reservation, a scan at the door
 * and the panel's day of blocks — so the question "is this shut?" is asked the same way five times
 * instead of five times differently.
 */
public final class SpaceClosures {

    private static final SpaceClosures NONE = new SpaceClosures(Map.of());

    private final Map<UUID, List<SpaceClosure>> bySpace;

    private SpaceClosures(Map<UUID, List<SpaceClosure>> bySpace) {
        this.bySpace = bySpace;
    }

    public static SpaceClosures of(List<SpaceClosure> closures) {
        return closures.isEmpty()
                ? NONE
                : new SpaceClosures(closures.stream().collect(Collectors.groupingBy(SpaceClosure::getSpaceId)));
    }

    public static SpaceClosures none() {
        return NONE;
    }

    /** The closure that shuts this block, if any: what the panel and the scanner report as the reason. */
    public Optional<SpaceClosure> covering(UUID spaceId, LocalDate date, LocalTime blockStart, LocalTime blockEnd) {
        return bySpace.getOrDefault(spaceId, List.of()).stream()
                .filter(closure -> closure.coversBlock(date, blockStart, blockEnd))
                .findFirst();
    }

    public boolean shut(UUID spaceId, LocalDate date, LocalTime blockStart, LocalTime blockEnd) {
        return covering(spaceId, date, blockStart, blockEnd).isPresent();
    }

    /**
     * Whether the space is shut at this instant, which is what the catalog calls "under
     * maintenance": one flag for a person deciding whether to walk over there now.
     */
    public boolean shutAt(UUID spaceId, LocalDateTime instant) {
        return bySpace.getOrDefault(spaceId, List.of()).stream().anyMatch(closure -> closure.coversInstant(instant));
    }
}
