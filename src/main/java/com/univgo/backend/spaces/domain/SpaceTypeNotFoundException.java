package com.univgo.backend.spaces.domain;

import java.util.UUID;

public class SpaceTypeNotFoundException extends RuntimeException {

    public SpaceTypeNotFoundException(UUID spaceTypeId) {
        super("Space type not found: " + spaceTypeId);
    }
}
