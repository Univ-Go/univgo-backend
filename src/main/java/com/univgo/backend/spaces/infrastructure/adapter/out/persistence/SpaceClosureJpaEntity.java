package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.domain.ClosureReason;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "space_closures")
public class SpaceClosureJpaEntity {

    @Id
    private UUID id;

    @Column(name = "space_id", nullable = false)
    private UUID spaceId;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    /** Same arrangement the reservation's own enums use: a converter writes the lower-case label,
     *  and {@code stringtype=unspecified} in the JDBC URL lets Postgres cast it to its enum type. */
    @Convert(converter = ClosureReasonConverter.class)
    @Column(nullable = false)
    private ClosureReason reason;

    @Column
    private String details;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reverted_at")
    private LocalDateTime revertedAt;

    @Column(name = "reverted_by")
    private UUID revertedBy;

    protected SpaceClosureJpaEntity() {
    }

    public SpaceClosureJpaEntity(
            UUID id,
            UUID spaceId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            ClosureReason reason,
            String details,
            UUID createdBy,
            LocalDateTime createdAt,
            LocalDateTime revertedAt,
            UUID revertedBy) {
        this.id = id;
        this.spaceId = spaceId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.reason = reason;
        this.details = details;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.revertedAt = revertedAt;
        this.revertedBy = revertedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public ClosureReason getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRevertedAt() {
        return revertedAt;
    }

    public UUID getRevertedBy() {
        return revertedBy;
    }
}
