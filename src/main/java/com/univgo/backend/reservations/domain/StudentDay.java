package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.TimeBlock;
import java.util.List;
import java.util.UUID;

/**
 * The reservations a student already holds today, wherever they are. It answers the two questions
 * that keep a person from booking themselves into a conflict: whether a candidate block clashes
 * with something they already hold, and whether they have used up their allowance at one space.
 */
public final class StudentDay {

    private final List<Reservation> reservations;

    private StudentDay(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public static StudentDay of(List<Reservation> reservations) {
        return new StudentDay(reservations);
    }

    /** A person cannot be in two places at once, whichever space the other reservation is for. */
    public boolean overlaps(TimeBlock block) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getBlockStart().isBefore(block.end())
                        && reservation.getBlockEnd().isAfter(block.start()));
    }

    public long heldAt(UUID spaceId) {
        return reservations.stream().filter(reservation -> reservation.getSpaceId().equals(spaceId)).count();
    }

    public boolean hasReachedLimitFor(UUID spaceId, InstitutionConfig config) {
        return heldAt(spaceId) >= config.getReservationsPerSpacePerDay();
    }
}
