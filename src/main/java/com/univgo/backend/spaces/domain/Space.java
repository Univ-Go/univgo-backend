package com.univgo.backend.spaces.domain;

import java.util.UUID;

public class Space {

    private final UUID id;
    private final String name;
    private final int capacity;
    private final UUID spaceTypeId;
    private final boolean underMaintenance;

    public Space(UUID id, String name, int capacity, UUID spaceTypeId, boolean underMaintenance) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.spaceTypeId = spaceTypeId;
        this.underMaintenance = underMaintenance;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public UUID getSpaceTypeId() {
        return spaceTypeId;
    }

    public boolean isUnderMaintenance() {
        return underMaintenance;
    }
}
