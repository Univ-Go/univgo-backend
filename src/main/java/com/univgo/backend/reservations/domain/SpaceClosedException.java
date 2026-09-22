package com.univgo.backend.reservations.domain;

import java.util.UUID;

public class SpaceClosedException extends RuntimeException {

    public SpaceClosedException(UUID spaceId) {
        super("Space " + spaceId + " is closed for the requested block");
    }
}
