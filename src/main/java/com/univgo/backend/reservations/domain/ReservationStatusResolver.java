package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceClosures;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * The reservation's own clock, read against the closures of the space it is for.
 *
 * <p>{@link Reservation#stateAt} stays a pure function of the reservation and knows nothing about
 * closures — it cannot, a closure is a fact about the space. This is where the two meet, and it is
 * still a pure function of an explicit "now", so the spec's "el estado se calcula, no se guarda"
 * holds for suspension too: nothing is written when a space closes, and nothing has to be written
 * back when it reopens.
 */
public final class ReservationStatusResolver {

    private ReservationStatusResolver() {
    }

    public static ReservationStatus resolve(
            Reservation reservation, SpaceClosures closures, LocalDateTime now, InstitutionConfig config) {
        ReservationState state = reservation.stateAt(now, config.tolerance(), config.minUsage());

        // A cancellation is a fact somebody wrote down; a closure cannot undo it.
        if (state == ReservationState.CANCELLED) {
            return ReservationStatus.cancelledBy(reservation.getCancelledBy());
        }

        // Already inside: they got in before the door shut, and the block is theirs.
        if (reservation.getCheckedInAt() != null) {
            return ReservationStatus.of(state);
        }

        Optional<SpaceClosure> closure = closures.covering(
                reservation.getSpaceId(),
                reservation.getReservationDate(),
                reservation.getBlockStart(),
                reservation.getBlockEnd());

        if (closure.isEmpty()) {
            return ReservationStatus.of(state);
        }

        // Expiring is not having turned up, and here the door was shut: a block that ends under a
        // closure ends as cancelled by the space, with its reason, and gives the day's booking back
        // (spec §12). While the block is still to come, the reservation is only suspended, and a
        // reverted closure hands it straight back.
        LocalDateTime blockEnd = LocalDateTime.of(reservation.getReservationDate(), reservation.getBlockEnd());

        return now.isBefore(blockEnd)
                ? ReservationStatus.suspendedBy(closure.get().getReason())
                : ReservationStatus.closedBy(closure.get().getReason());
    }
}
