package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.ClosureReason;

/**
 * A reservation's state together with the two things that explain it when somebody else decided it:
 * who cancelled, and —when a closure is what ended it— why the space was shut.
 *
 * <p>A student who gave up their own booking and one whose block was closed by the university both
 * read "cancelled" without this, and only one of the two is their doing.
 */
public record ReservationStatus(ReservationState state, CancelledBy cancelledBy, ClosureReason closureReason) {

    public static ReservationStatus of(ReservationState state) {
        return new ReservationStatus(state, null, null);
    }

    public static ReservationStatus cancelledBy(CancelledBy actor) {
        return new ReservationStatus(ReservationState.CANCELLED, actor, null);
    }

    public static ReservationStatus suspendedBy(ClosureReason reason) {
        return new ReservationStatus(ReservationState.SUSPENDED, null, reason);
    }

    public static ReservationStatus closedBy(ClosureReason reason) {
        return new ReservationStatus(ReservationState.CANCELLED, CancelledBy.ADMIN, reason);
    }
}
