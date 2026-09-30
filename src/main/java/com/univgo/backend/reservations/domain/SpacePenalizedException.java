package com.univgo.backend.reservations.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpacePenalizedException extends RuntimeException {

    public SpacePenalizedException(UUID spaceId, LocalDateTime penaltyEndsAt) {
        super("Student is penalized from space " + spaceId + " until " + penaltyEndsAt
                + " for letting a reservation expire");
    }
}
