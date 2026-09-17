package com.univgo.backend.spaces.domain;

import java.util.UUID;

public class Space {

    private final UUID id;
    private final String name;
    private final String location;
    private final int capacity;
    private final UUID spaceTypeId;
    private final SpaceCategory category;
    private final boolean underMaintenance;

    public Space(
            UUID id,
            String name,
            String location,
            int capacity,
            UUID spaceTypeId,
            SpaceCategory category,
            boolean underMaintenance) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.spaceTypeId = spaceTypeId;
        this.category = category;
        this.underMaintenance = underMaintenance;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public UUID getSpaceTypeId() {
        return spaceTypeId;
    }

    public SpaceCategory getCategory() {
        return category;
    }

    public boolean isUnderMaintenance() {
        return underMaintenance;
    }
}
