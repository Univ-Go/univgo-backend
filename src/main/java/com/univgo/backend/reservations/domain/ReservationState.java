package com.univgo.backend.reservations.domain;

/**
 * The five states from the functional spec. Never persisted — always the
 * result of {@link ReservationTimingCalculator#stateAt} applied to the
 * current instant.
 */
public enum ReservationState {
    RESERVED,
    /** Its block falls inside a closure of the space: it holds its place, and comes back if the
     *  closure is reverted. Never stored — {@link ReservationStatusResolver} derives it. */
    SUSPENDED,
    IN_PROGRESS,
    FINISHED,
    EXPIRED,
    CANCELLED
}
