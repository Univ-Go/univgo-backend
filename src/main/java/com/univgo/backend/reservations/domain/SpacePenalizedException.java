package com.univgo.backend.reservations.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class SpacePenalizedException extends RuntimeException {

    private final UUID spaceId;
    private final LocalDateTime penalizedUntil;

    public SpacePenalizedException(UUID spaceId, LocalDateTime penalizedUntil) {
        super("Student is penalized from space " + spaceId + " until " + penalizedUntil
                + " for letting a reservation expire");
        this.spaceId = spaceId;
        this.penalizedUntil = penalizedUntil;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public LocalDateTime getPenalizedUntil() {
        return penalizedUntil;
    }
}
