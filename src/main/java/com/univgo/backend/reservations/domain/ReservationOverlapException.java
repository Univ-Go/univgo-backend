package com.univgo.backend.reservations.domain;

public class ReservationOverlapException extends RuntimeException {

    public ReservationOverlapException() {
        super("You already have a reservation that overlaps this time range");
    }
}
