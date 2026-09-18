package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalDateTime;

/**
 * A block decorated with everything the catalog/step-2-3 screens need to
 * decide whether to offer it, and — per the spec's explicit design decision —
 * the check-in deadline the student must see *before* confirming, computed as
 * if they reserved right now.
 */
public record BlockAvailability(
        TimeBlock block,
        int capacity,
        int occupied,
        int free,
        boolean offered,
        boolean alreadyReservedByUserToday,
        boolean overlapsUserReservation,
        /** The space is shut for this block, which is why it is not on offer (spec §12). */
        boolean closed,
        /** Why it is shut, so the student reads "cerrado por mantenimiento" and not a blank refusal. */
        ClosureReason closureReason,
        LocalDateTime previewCheckInOpensAt,
        LocalDateTime previewCheckInClosesAt) {
}
