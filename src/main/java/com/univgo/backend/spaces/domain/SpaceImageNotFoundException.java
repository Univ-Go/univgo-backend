package com.univgo.backend.spaces.domain;

import java.util.UUID;

public class SpaceImageNotFoundException extends RuntimeException {

    public SpaceImageNotFoundException(UUID imageId) {
        super("Space image not found: " + imageId);
    }
}
