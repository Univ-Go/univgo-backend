package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    /**
     * A Postgres {@code text[]}, not a join table: a rule has no identity of its own and is never
     * queried by itself, so the column is the list and its order is the reading order.
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private String[] rules;

    /**
     * Read-only view of the type row, for its category. The writable UUID column above stays the
     * one thing that decides which type a space points at, so this mapping can never rewrite it.
     */
    @ManyToOne
    @JoinColumn(name = "space_type_id", insertable = false, updatable = false)
    private SpaceTypeJpaEntity spaceType;

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

    public String getDescription() {
        return description;
    }

    public String[] getRules() {
        return rules.clone();
    }

    public SpaceTypeJpaEntity getSpaceType() {
        return spaceType;
    }

}
