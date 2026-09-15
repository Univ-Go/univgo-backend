package com.univgo.backend.reservations.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class BlockCapacityFullException extends RuntimeException {

    public BlockCapacityFullException(UUID spaceId, LocalDate date, LocalTime start, LocalTime end) {
        super("Space " + spaceId + " has no free plazas left for the " + start + "-" + end + " block on " + date);
    }
}
