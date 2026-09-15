package com.univgo.backend.reservations.domain;

public class InvalidReservationStateException extends RuntimeException {

    public InvalidReservationStateException(ReservationState current) {
        super("Reservation is " + current + " and cannot be cancelled");
    }
}
