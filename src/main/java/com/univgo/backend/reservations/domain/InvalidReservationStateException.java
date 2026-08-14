package com.univgo.backend.reservations.domain;

public class InvalidReservationStateException extends RuntimeException {

    public InvalidReservationStateException(ReservationStatus current, ReservationStatus attempted) {
        super("Cannot change reservation from " + current + " to " + attempted);
    }
}
