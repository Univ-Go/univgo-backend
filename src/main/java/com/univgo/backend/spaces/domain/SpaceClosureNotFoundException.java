package com.univgo.backend.spaces.domain;

import java.util.UUID;

public class SpaceClosureNotFoundException extends RuntimeException {

    public SpaceClosureNotFoundException(UUID closureId) {
        super("Closure " + closureId + " does not exist");
    }
}
