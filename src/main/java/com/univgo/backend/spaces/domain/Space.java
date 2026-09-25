package com.univgo.backend.spaces.domain;

import java.util.List;
import java.util.UUID;

public class Space {

    private final UUID id;
    private final String name;
    private final String location;
    private final int capacity;
    private final UUID spaceTypeId;
    private final SpaceCategory category;
    private final SpaceDetails details;

    /**
     * Whether the space is open is not one of its properties: it is whether a closure covers the
     * moment being asked about (spec §12), and that is a question about time. `SpaceClosures`
     * answers it.
     */
    public Space(
            UUID id,
            String name,
            String location,
            int capacity,
            UUID spaceTypeId,
            SpaceCategory category,
            SpaceDetails details) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.spaceTypeId = spaceTypeId;
        this.category = category;
        this.details = details;
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

    public String getDescription() {
        return details.description();
    }

    /**
     * What a student must read before booking this space, in reading order. Per space and not per
     * category: two courts of the same type share a taxonomy, not a set of instructions. Empty for
     * a space whose rules nobody has written yet, which the views read as "no section".
     */
    public List<String> getRules() {
        return details.rules();
    }

}
