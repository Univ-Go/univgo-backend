package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A day's active reservations, indexed by the block they belong to. It exists so that reading a
 * day is one query instead of one per block: the grouping a database would do per request is done
 * once here, and every block then asks an in-memory map.
 */
public final class BlockReservations {

    private record Key(UUID spaceId, LocalTime start, LocalTime end) {
    }

    private final Map<Key, List<Reservation>> byBlock;

    private BlockReservations(Map<Key, List<Reservation>> byBlock) {
        this.byBlock = byBlock;
    }

    public static BlockReservations of(List<Reservation> activeReservations) {
        return new BlockReservations(activeReservations.stream()
                .collect(Collectors.groupingBy(reservation -> new Key(
                        reservation.getSpaceId(), reservation.getBlockStart(), reservation.getBlockEnd()))));
    }

    public List<Reservation> of(UUID spaceId, TimeBlock block) {
        return byBlock.getOrDefault(new Key(spaceId, block.start(), block.end()), List.of());
    }
}
