package com.univgo.backend.reservations.domain;

import java.time.Duration;
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

    public static long countOccupiedPlazas(
            List<Reservation> activeReservationsInBlock, LocalDateTime now, Duration tolerance, Duration minUsage) {
        return activeReservationsInBlock.stream()
                .map(r -> r.stateAt(now, tolerance, minUsage))
                .filter(state -> state == ReservationState.RESERVED || state == ReservationState.IN_PROGRESS)
                .count();
    }
}
