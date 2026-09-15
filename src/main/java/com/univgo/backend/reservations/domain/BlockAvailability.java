package com.univgo.backend.reservations.domain;

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
        LocalDateTime previewCheckInOpensAt,
        LocalDateTime previewCheckInClosesAt) {
}
