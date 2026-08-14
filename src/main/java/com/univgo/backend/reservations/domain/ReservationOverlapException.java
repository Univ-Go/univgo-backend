package com.univgo.backend.reservations.domain;

public class ReservationOverlapException extends RuntimeException {

    public ReservationOverlapException() {
        super("Space already reserved for the selected date and time range");
    }
}
