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
    private final String description;
    private final List<String> rules;

    /**
     * Whether the space is open is not one of its properties: it is whether a closure covers the
     * moment being asked about (spec §12), and that is a question about time. `SpaceClosures`
     * answers it.
     */
    private Space(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.location = builder.location;
        this.capacity = builder.capacity;
        this.spaceTypeId = builder.spaceTypeId;
        this.category = builder.category;
        this.description = builder.description;
        this.rules = List.copyOf(builder.rules);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID id;
        private String name;
        private String location;
        private int capacity;
        private UUID spaceTypeId;
        private SpaceCategory category;
        private String description;
        private List<String> rules = List.of();

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder spaceTypeId(UUID spaceTypeId) {
            this.spaceTypeId = spaceTypeId;
            return this;
        }

        public Builder category(SpaceCategory category) {
            this.category = category;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder rules(List<String> rules) {
            this.rules = rules;
            return this;
        }

        public Space build() {
            return new Space(this);
        }
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
        return description;
    }

    /**
     * What a student must read before booking this space, in reading order. Per space and not per
     * category: two courts of the same type share a taxonomy, not a set of instructions. Empty for
     * a space whose rules nobody has written yet, which the views read as "no section".
     */
    public List<String> getRules() {
        return rules;
    }

}
