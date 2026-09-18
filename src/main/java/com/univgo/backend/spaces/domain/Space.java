package com.univgo.backend.spaces.domain;

import java.util.UUID;

public class Space {

    private final UUID id;
    private final String name;
    private final String location;
    private final int capacity;
    private final UUID spaceTypeId;
    private final SpaceCategory category;

    /**
     * Whether the space is open is not one of its properties: it is whether a closure covers the
     * moment being asked about (spec §12), and that is a question about time. `SpaceClosures`
     * answers it.
     */
    public Space(UUID id, String name, String location, int capacity, UUID spaceTypeId, SpaceCategory category) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.spaceTypeId = spaceTypeId;
        this.category = category;
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

}
