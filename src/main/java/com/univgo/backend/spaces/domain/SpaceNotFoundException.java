package com.univgo.backend.spaces.domain;

import java.util.UUID;

public class SpaceNotFoundException extends RuntimeException {

    public SpaceNotFoundException(UUID spaceId) {
        super("Space not found: " + spaceId);
    }
}
