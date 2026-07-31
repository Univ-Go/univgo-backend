package com.univgo.backend.reservations.domain;

import java.util.UUID;

public class SpaceNotFoundException extends RuntimeException {

    public SpaceNotFoundException(UUID spaceId) {
        super("Space not found: " + spaceId);
    }
}
