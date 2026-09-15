package com.univgo.backend.reservations.domain;

import java.util.UUID;

public class CannotCancelInProgressReservationException extends RuntimeException {

    public CannotCancelInProgressReservationException(UUID reservationId) {
        super("Reservation " + reservationId + " is already in progress and cannot be cancelled; it ends on its own");
    }
}
