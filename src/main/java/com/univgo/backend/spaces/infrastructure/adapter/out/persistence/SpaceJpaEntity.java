package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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

    /**
     * When the space was retired, and by whom. Readable only from inside this package — the
     * getter is deliberately not public — because whether a space is archived is a question the
     * repository answers through {@code findAllActive} and the panel's own read model, not something
     * a caller holding a {@code Space} can branch on. Keeping it off the domain object is what stops
     * every consumer from having to remember the check.
     */
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "archived_by")
    private UUID archivedBy;

    protected SpaceJpaEntity() {
    }

    /**
     * Writing a space means replacing its row whole, the same way {@code SpaceClosureJpaEntity}
     * does it: an all-args constructor and no setters, so a loaded entity can never be mutated in
     * place. {@code spaceType} is left null here on purpose — its mapping is read-only, so the
     * {@code space_type_id} column above remains the single thing that decides the type.
     */
    public SpaceJpaEntity(
            UUID id,
            String name,
            String location,
            Integer capacity,
            UUID spaceTypeId,
            String description,
            String[] rules) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.spaceTypeId = spaceTypeId;
        this.description = description;
        this.rules = rules.clone();
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

    LocalDateTime getArchivedAt() {
        return archivedAt;
    }

}
