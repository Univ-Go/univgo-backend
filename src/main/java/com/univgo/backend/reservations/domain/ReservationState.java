package com.univgo.backend.reservations.domain;

/**
 * The five states from the functional spec. Never persisted — always the
 * result of {@link ReservationTimingCalculator#stateAt} applied to the
 * current instant.
 */
public enum ReservationState {
    RESERVED,
    IN_PROGRESS,
    FINISHED,
    EXPIRED,
    CANCELLED
}
