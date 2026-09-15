package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "spaces")
public class SpaceJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "space_type_id", nullable = false)
    private UUID spaceTypeId;

    @Column(name = "under_maintenance", nullable = false)
    private boolean underMaintenance;

    protected SpaceJpaEntity() {
    }

    public SpaceJpaEntity(UUID id, String name, Integer capacity, UUID spaceTypeId, boolean underMaintenance) {
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

    public Integer getCapacity() {
        return capacity;
    }

    public UUID getSpaceTypeId() {
        return spaceTypeId;
    }

    public boolean isUnderMaintenance() {
        return underMaintenance;
    }
}
