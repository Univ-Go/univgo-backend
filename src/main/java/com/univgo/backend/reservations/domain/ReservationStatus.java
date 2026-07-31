package com.univgo.backend.reservations.domain;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    CANCELLED_BY_ADMIN,
    CANCELLED_BY_USER,
    COMPLETED,
    EXPIRED
}
