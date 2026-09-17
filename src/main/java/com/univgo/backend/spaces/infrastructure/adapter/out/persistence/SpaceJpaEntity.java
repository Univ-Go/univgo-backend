package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private String location;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "space_type_id", nullable = false)
    private UUID spaceTypeId;

    /**
     * Read-only view of the type row, for its category. The writable UUID column above stays the
     * one thing that decides which type a space points at, so this mapping can never rewrite it.
     */
    @ManyToOne
    @JoinColumn(name = "space_type_id", insertable = false, updatable = false)
    private SpaceTypeJpaEntity spaceType;

    @Column(name = "under_maintenance", nullable = false)
    private boolean underMaintenance;

    protected SpaceJpaEntity() {
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

    public Integer getCapacity() {
        return capacity;
    }

    public UUID getSpaceTypeId() {
        return spaceTypeId;
    }

    public SpaceTypeJpaEntity getSpaceType() {
        return spaceType;
    }

    public boolean isUnderMaintenance() {
        return underMaintenance;
    }

    public void setUnderMaintenance(boolean underMaintenance) {
        this.underMaintenance = underMaintenance;
    }
}
