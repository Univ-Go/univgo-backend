package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.SpaceClosures;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Whether a student is still barred from one space, read from their recent reservations for it. */
public final class SpacePenalty {

    private SpacePenalty() {
    }

    /**
     * The latest instant a penalty still running at {@code now} lifts. {@code candidates} are the
     * student's non-cancelled reservations for that one space, and only the ones that resolve to
     * {@link ReservationState#EXPIRED} count, so a closure-caused non-show never penalizes.
     */
    public static Optional<LocalDateTime> activeUntil(
            List<Reservation> candidates, SpaceClosures closures, LocalDateTime now, InstitutionConfig config) {
        return candidates.stream()
                .filter(reservation -> ReservationStatusResolver.resolve(reservation, closures, now, config).state()
                        == ReservationState.EXPIRED)
                .map(Reservation::penaltyEndsAt)
                .filter(now::isBefore)
                .max(Comparator.naturalOrder());
    }
}
