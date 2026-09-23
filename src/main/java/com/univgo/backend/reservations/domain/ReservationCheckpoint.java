package com.univgo.backend.reservations.domain;

import java.time.LocalDateTime;

/** A reservation's check-in and cancellation facts — the two things a human action can record after creation. */
public record ReservationCheckpoint(LocalDateTime checkedInAt, LocalDateTime cancelledAt, CancelledBy cancelledBy) {

    public static ReservationCheckpoint initial() {
        return new ReservationCheckpoint(null, null, null);
    }

    public ReservationCheckpoint withCheckIn(LocalDateTime now) {
        return new ReservationCheckpoint(now, cancelledAt, cancelledBy);
    }

    public ReservationCheckpoint withCancellation(CancelledBy actor, LocalDateTime now) {
        return new ReservationCheckpoint(checkedInAt, now, actor);
    }
}
