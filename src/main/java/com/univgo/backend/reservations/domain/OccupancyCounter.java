package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.SpaceClosures;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Aforo isn't a stored counter — it's "reservas reservadas + reservas en
 * curso" for a block, recomputed on the fly, so a plaza frees up the instant
 * a reservation expires or is cancelled (spec: "liberar una plaza no es una
 * acción, es una consecuencia").
 */
public final class OccupancyCounter {

    private OccupancyCounter() {
    }

    /**
     * A suspended reservation still holds its plaza: that is what lets a reverted closure hand it
     * back untouched. It costs nobody a seat, because a space that is shut offers no blocks.
     */
    public static long countOccupiedPlazas(
            List<Reservation> activeReservationsInBlock,
            SpaceClosures closures,
            LocalDateTime now,
            InstitutionConfig config) {
        return activeReservationsInBlock.stream()
                .map(r -> ReservationStatusResolver.resolve(r, closures, now, config).state())
                .filter(state -> state == ReservationState.RESERVED
                        || state == ReservationState.IN_PROGRESS
                        || state == ReservationState.SUSPENDED)
                .count();
    }
}
