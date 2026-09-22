package com.univgo.backend.reservations.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class CancellationWindowClosedException extends RuntimeException {

    public CancellationWindowClosedException(UUID reservationId, LocalDateTime blockStart) {
        super("Reservation " + reservationId + " starts at " + blockStart
                + " and can no longer be cancelled; it is less than one hour away. Attend the reservation or let it expire.");
    }
}
