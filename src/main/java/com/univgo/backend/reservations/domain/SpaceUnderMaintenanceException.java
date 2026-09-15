package com.univgo.backend.reservations.domain;

import java.util.UUID;

public class SpaceUnderMaintenanceException extends RuntimeException {

    public SpaceUnderMaintenanceException(UUID spaceId) {
        super("Space " + spaceId + " is under maintenance and is not offering blocks");
    }
}
