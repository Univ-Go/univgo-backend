package com.univgo.backend.reservations.domain;

import java.time.LocalDate;
import java.util.UUID;

public class SpaceAlreadyReservedTodayException extends RuntimeException {

    public SpaceAlreadyReservedTodayException(UUID spaceId, LocalDate date) {
        super("Space " + spaceId + " was already reserved by this student on " + date);
    }
}
